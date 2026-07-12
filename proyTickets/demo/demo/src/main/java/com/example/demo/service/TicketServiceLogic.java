package com.example.demo.service;

import com.example.demo.entity.Ticket;
import com.example.demo.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TicketServiceLogic {

    private final TicketRepository repository;
    private final RestTemplate restTemplate; 
    private final AtomicInteger sequence = new AtomicInteger(1);

    public TicketServiceLogic(TicketRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public Ticket crearTicket(Ticket ticketRequest) {
        String fecha = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String seqFormat = String.format("%04d", sequence.getAndIncrement());
        ticketRequest.setTicketCode("TICKET-" + fecha + "-" + seqFormat);

        long currentTime = System.currentTimeMillis();
        ticketRequest.setTimestampCreacion(currentTime);
        ticketRequest.setTimestampActualizacion(currentTime);
        
        if (ticketRequest.getClienteId() == null || ticketRequest.getClienteId().trim().isEmpty()) {
            ticketRequest.setClienteId("CLI-ANONIMO"); // Cliente de paso
            System.out.println("[INFO] Pedido sin cliente. Se auto-asignó el ID: CLI-ANONIMO");
        }

        // EVIDENCIA 1: Pedido antes de la falla
        ticketRequest.setEstado("PENDIENTE_DE_ASIGNACION");
        ticketRequest = repository.save(ticketRequest);
        System.out.println("\n[NUEVO PEDIDO/CHECKPOINT] Intentando procesar " + ticketRequest.getTicketCode() + " ,registrado en el LOG");

        boolean asignacionExitosa = false;
        int MAX_REINTENTOS = 3;

        // EVIDENCIA 2: Reintentos limitados y timeout/falla
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                System.out.println("[INTENTO " + intento + "/" + MAX_REINTENTOS + "] Solicitando asignación al assignment-service...");
                String urlAsignacion = "http://localhost:8082/api/assignment/auto/" + ticketRequest.getTicketCode();
                
                String mozoAsignado = restTemplate.postForObject(urlAsignacion, null, String.class);
                
                ticketRequest.setOperadorAsignadoId(mozoAsignado);
                ticketRequest.setEstado("ASIGNADO");
                System.out.println("ÉXITO: El ticket se asignó a: " + mozoAsignado);
                asignacionExitosa = true;
                break; // Bucle roto=exito

            } catch (Exception e) {
                System.out.println("[FALLA DETECTADA] Timeout o error de conexión con assignment-service.");
                try { Thread.sleep(1000); } catch (InterruptedException ie) {} // Pausa de 1 seg entre reintentos
            }
        }

        // EVIDENCIA 3: Fallback y estado actualizado
        if (!asignacionExitosa) {
            System.out.println("[CAMBIO DE ESTRATEGIA] Reintentos agotados. Activando Fallback...");
            ticketRequest.setEstado("PENDIENTE");
            System.out.println("[FALLBACK] assignment-service caído. El ticket se guardó localmente con estado " + ticketRequest.getEstado());
        }else{
            repository.save(ticketRequest);
        }

        return ticketRequest;
    }

    @Scheduled(fixedDelay = 10000) // Se ejecuta cada 10seg
    public void reconciliarTicketsPendientes() {
        // Buscar si hay tickets en modo degradado
        List<Ticket> pendientes = repository.findByEstado("PENDIENTE_DE_ASIGNACION");
        
        if (pendientes.isEmpty()) {
            return; // Si no hay nada por recuperar, no hace nada
        }

        System.out.println("\n[RECUPERACIÓN INICIADA] Se encontraron " + pendientes.size() + " pedidos pendientes de sincronización. Intentando recuperar...");

        for (Ticket ticket : pendientes) {
            if (ticket.getOperadorAsignadoId() != null) continue;// Validamos que no hay mozo asignado anteriormente/evita duplicidad

            try {
                System.out.println("[ROLL-FORWARD] Intentando conectar de nuevo por la red para el pedido " + ticket.getTicketCode() + "...");
                String urlAsignacion = "http://localhost:8082/api/assignment/auto/" + ticket.getTicketCode();
                // Intentamos conectar de nuevo con el servicio principal por la red
                String mozoAsignado = restTemplate.postForObject(urlAsignacion, null, String.class);
                
                // Si revive, actualizamos los datos del ticket antiguo
                ticket.setOperadorAsignadoId(mozoAsignado);
                ticket.setEstado("ASIGNADO");
                ticket.setTimestampActualizacion(System.currentTimeMillis());
                repository.save(ticket);
                
                System.out.println("[RECUPERACIÓN EXITOSA] El antiguo pedido " + ticket.getTicketCode() + " fue sincronizado automáticamente y asignado al mozo " + mozoAsignado);
                
            } catch (Exception e) {
                System.out.println("[RECUPERACIÓN FALLIDA] El servicio de asignación sigue caído. Se reintentará en 10 segundos ");
            }
        }
    }

}
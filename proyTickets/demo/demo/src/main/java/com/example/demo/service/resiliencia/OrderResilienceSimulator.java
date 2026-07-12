package com.example.demo.service.resiliencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeoutException;

@Service
public class OrderResilienceSimulator {

    private static final Logger log = LoggerFactory.getLogger(OrderResilienceSimulator.class);
    
    // Parámetros de tolerancia a fallas
    private static final int MAX_REINTENTOS = 3;
    private static final int TIMEOUT_MS = 2000;

    public String confirmarPedido(String idPedido, String nuevoEstado) {
        log.info("[SOLICITUD ENVIADA] Mozo intenta cambiar pedido {} a estado '{}'", idPedido, nuevoEstado);

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                log.info("[INTENTO {}/{}] Comunicando con Servicio Principal (Ticket-Service)...", intento, MAX_REINTENTOS);
                return llamarNodoPrincipal(idPedido, nuevoEstado);
                
            } catch (TimeoutException e) {
                log.warn("[FALLA DETECTADA] Timeout en Servicio Principal. Excedió los {}ms.", TIMEOUT_MS);
            } catch (Exception e) {
                log.error("[FALLA DETECTADA] Error de conectividad: {}", e.getMessage());
            }
        }

        log.error("[CAMBIO DE ESTRATEGIA] Reintentos limitados agotados. Activando Fallback...");
        return activarModoDegradado(idPedido, nuevoEstado);
    }

    private String llamarNodoPrincipal(String idPedido, String estado) throws TimeoutException {
        simularDemoraExcesiva(5000); 
        return "EXITO: Pedido " + idPedido + " actualizado a " + estado + " en BD Principal.";
    }

    private String activarModoDegradado(String idPedido, String estado) {
        log.info("[FALLBACK] Redirigiendo a Cola Temporal (Modo Degradado)...");
        log.info("[RESULTADO FINAL] Pedido {} guardado en caché local. Se sincronizará con cocina cuando el servicio vuelva.", idPedido);
        
        return "MODO DEGRADADO: Pedido aceptado temporalmente.";
    }

    private void simularDemoraExcesiva(int milisegundos) throws TimeoutException {
        try {
            Thread.sleep(milisegundos);
            if (milisegundos > TIMEOUT_MS) {
                throw new TimeoutException("Demora de respuesta inaceptable");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
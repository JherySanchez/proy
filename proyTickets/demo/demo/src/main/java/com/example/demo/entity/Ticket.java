package com.example.demo.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad principal del ticket-service.
 * Representa un pedido en el sistema distribuido.
 */
@Entity
@Table(name = "tickets")
public class Ticket implements Serializable {

    // El ID debe ser autogenerado por la BD internamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador único de negocio exigido (ej. TICKET-20260616-0001)
    @Column(name = "ticket_code", unique = true, nullable = false)
    private String ticketCode;

    @Column(name = "mesa_id", nullable = false)
    private String mesaId; // ej. MESA-05

    @Column(name = "cliente_id")
    private String clienteId;

    @Column(name = "operador_asignado_id")
    private String operadorAsignadoId; // ID del mozo/cocinero (USR-MOZO-001)

    @Column(name = "detalle_pedido", length = 500)
    private String detallePedido; // JSON o texto con los platos

    // Estado del ticket: PENDIENTE, EN_PREPARACIÓN, LISTO, ENTREGADO, PAGADO
    @Column(name = "estado", nullable = false)
    private String estado;

    // Sincronización de relojes (System.currentTimeMillis)
    @Column(name = "timestamp_creacion")
    private Long timestampCreacion;

    @Column(name = "timestamp_actualizacion")
    private Long timestampActualizacion;

    // Constructor vacío requerido por JPA (Hibernate)
    public Ticket() {
    }

    // Getters y Setters manuales (Si tienes Lombok instalado, podríamos usar @Data y borrar esto)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getOperadorAsignadoId() { return operadorAsignadoId; }
    public void setOperadorAsignadoId(String operadorAsignadoId) { this.operadorAsignadoId = operadorAsignadoId; }

    public String getDetallePedido() { return detallePedido; }
    public void setDetallePedido(String detallePedido) { this.detallePedido = detallePedido; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getTimestampCreacion() { return timestampCreacion; }
    public void setTimestampCreacion(Long timestampCreacion) { this.timestampCreacion = timestampCreacion; }

    public Long getTimestampActualizacion() { return timestampActualizacion; }
    public void setTimestampActualizacion(Long timestampActualizacion) { this.timestampActualizacion = timestampActualizacion; }
}


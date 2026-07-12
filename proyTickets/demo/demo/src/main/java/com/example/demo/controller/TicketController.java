package com.example.demo.controller;

import com.example.demo.entity.Ticket;
import com.example.demo.service.TicketServiceLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketServiceLogic service;

    public TicketController(TicketServiceLogic service) {
        this.service = service;
    }

    // Endpoint para que el cliente o mozo genere un pedido
    @PostMapping
    public ResponseEntity<Ticket> registrarPedido(@RequestBody Ticket ticket) {
        Ticket nuevoTicket = service.crearTicket(ticket);
        return ResponseEntity.ok(nuevoTicket);
    }
}
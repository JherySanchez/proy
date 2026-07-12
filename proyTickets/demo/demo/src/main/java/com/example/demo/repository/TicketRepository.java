package com.example.demo.repository;

import com.example.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Spring Data JPA ya nos provee métodos como save(), findAll(), findById() mágicamente.
    List<Ticket> findByEstado(String estado);
}

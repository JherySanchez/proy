package com.example.demo.controller;

import com.example.demo.service.resiliencia.OrderResilienceSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulador")
public class SimulacionFallasController {

    @Autowired
    private OrderResilienceSimulator simulador;

    @PostMapping("/falla-ticket")
    public String ejecutarSimulacion(
            @RequestParam(defaultValue = "TKT-001") String idPedido, 
            @RequestParam(defaultValue = "EN_COCINA") String estado) {
        
        return simulador.confirmarPedido(idPedido, estado);
    }
}
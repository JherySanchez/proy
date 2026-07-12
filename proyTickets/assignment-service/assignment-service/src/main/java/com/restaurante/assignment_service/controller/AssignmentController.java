package com.restaurante.assignment_service.controller;

import com.restaurante.assignment_service.service.AssignmentLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {

    private final AssignmentLogic assignmentLogic;

    public AssignmentController(AssignmentLogic assignmentLogic) {
        this.assignmentLogic = assignmentLogic;
    }

    // Endpoint para probar el Round-Robin
    @PostMapping("/auto/{ticketCode}")
    public ResponseEntity<String> autoAssign(@PathVariable String ticketCode) {
        String asignadoA = assignmentLogic.asignarAutomaticamente(ticketCode);
        // ¡Cambio aquí! Devolvemos puramente el ID del mozo para facilitar la integración
        return ResponseEntity.ok(asignadoA); 
    }
    // Endpoint para probar la Exclusión Mutua
    @PostMapping("/manual/{ticketCode}")
    public ResponseEntity<String> manualAssign(@PathVariable String ticketCode, @RequestParam String idMozo) {
        boolean exito = assignmentLogic.intentarAsignacionManual(ticketCode, idMozo);
        if (exito) {
            return ResponseEntity.ok("Éxito: El ticket " + ticketCode + " fue asignado a " + idMozo);
        } else {
            return ResponseEntity.status(409).body("Conflicto: El ticket " + ticketCode + " ya está siendo procesado por otro mozo.");
        }
    }
    // Endpoint para probar el Caos (SIN exclusión mutua)
    @PostMapping("/manual-caos/{ticketCode}")
    public ResponseEntity<String> manualAssignCaos(@PathVariable String ticketCode, @RequestParam String idMozo) {
        assignmentLogic.asignacionManualSinBloqueo(ticketCode, idMozo);
        return ResponseEntity.ok("ALERTA: El ticket " + ticketCode + " fue modificado sin protección por " + idMozo);
    }
}
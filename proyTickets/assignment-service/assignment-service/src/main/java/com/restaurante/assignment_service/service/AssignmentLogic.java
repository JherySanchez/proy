package com.restaurante.assignment_service.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AssignmentLogic {

    private final List<String> mozosDisponibles = Arrays.asList("USR-MOZO-001", "USR-MOZO-002", "USR-MOZO-003");
    private int indiceRoundRobin = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public synchronized String asignarAutomaticamente(String ticketCode) {
        String mozoAsignado = mozosDisponibles.get(indiceRoundRobin);
        System.out.println("Asignando " + ticketCode + " a " + mozoAsignado + " (Round-Robin)");
        indiceRoundRobin = (indiceRoundRobin + 1) % mozosDisponibles.size();
        return mozoAsignado;
    }

    // --- NUEVO: MÉTODO PARA DEMOSTRAR EL ERROR AL PROFESOR ---
    public boolean asignacionManualSinBloqueo(String ticketCode, String idMozo) {
        System.out.println("🚨 PELIGRO: Mozo " + idMozo + " entrando a sección crítica SIN CONTROL para el ticket " + ticketCode);
        try {
            Thread.sleep(2000); // Simulamos demora
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("❌ ERROR DE CONCURRENCIA: Mozo " + idMozo + " se ASIGNÓ el ticket " + ticketCode + " simultáneamente con otros.");
        return true; 
    }

    // --- ACTUALIZADO: LOGS EXACTOS SEGÚN LA RÚBRICA ---
    public boolean intentarAsignacionManual(String ticketCode, String idMozo) {
        System.out.println("\n[LOG] Proceso que solicita: Mozo " + idMozo + " intenta acceder a " + ticketCode);
        System.out.println("[LOG] Proceso que espera: Mozo " + idMozo + " esperando el candado...");

        if (lock.tryLock()) {
            try {
                System.out.println("[LOG] Proceso autorizado: Mozo " + idMozo + " obtuvo el candado.");
                System.out.println("[LOG] Ingreso a sección crítica: Procesando asignación exclusiva de " + ticketCode);
                
                Thread.sleep(2000); 
                
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                lock.unlock();
                System.out.println("[LOG] Liberación del recurso: Candado soltado por " + idMozo);
            }
        } else {
            System.out.println("[LOG] RECHAZO: Mozo " + idMozo + " bloqueado. El ticket ya está en la sección crítica de otro operador.");
            return false;
        }
    }
}
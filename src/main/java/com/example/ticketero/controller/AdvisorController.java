package com.example.ticketero.controller;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.service.AdvisorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de asesores
 * Proporciona endpoints para consultar y modificar estados de asesores
 */
@RestController
@RequestMapping("/api/advisors")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"}, maxAge = 3600)
@Slf4j
public class AdvisorController {

    private final AdvisorService advisorService;

    /**
     * Obtiene todos los asesores del sistema
     * @return Lista completa de asesores
     */
    @GetMapping
    public ResponseEntity<List<Advisor>> obtenerTodosLosAsesores() {
        log.debug("Solicitando todos los asesores");
        try {
            List<Advisor> asesores = advisorService.obtenerTodosLosAsesores();
            log.debug("Retornando {} asesores", asesores.size());
            return ResponseEntity.ok(asesores);
        } catch (Exception e) {
            log.error("Error obteniendo todos los asesores: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene solo los asesores disponibles
     * @return Lista de asesores con estado AVAILABLE
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<Advisor>> obtenerAsesoresDisponibles() {
        log.debug("Solicitando asesores disponibles");
        try {
            List<Advisor> asesores = advisorService.obtenerAsesoresDisponibles();
            log.debug("Retornando {} asesores disponibles", asesores.size());
            return ResponseEntity.ok(asesores);
        } catch (Exception e) {
            log.error("Error obteniendo asesores disponibles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cambia el estado de un asesor
     * @param advisorId ID del asesor
     * @param estado Nuevo estado a asignar
     * @return Respuesta sin contenido si es exitoso
     */
    @PutMapping("/{advisorId}/estado")
    public ResponseEntity<Void> cambiarEstadoAsesor(
            @PathVariable Long advisorId, 
            @RequestParam AdvisorStatus estado) {
        
        if (advisorId <= 0) {
            log.warn("ID de asesor inválido: {}", advisorId);
            return ResponseEntity.badRequest().build();
        }
        
        if (estado == null) {
            log.warn("Estado no puede ser null para asesor {}", advisorId);
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Cambiando estado del asesor {} a {}", advisorId, estado);
        
        try {
            advisorService.cambiarEstadoAsesor(advisorId, estado);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación cambiando estado asesor {}: {}", advisorId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error cambiando estado asesor {}: {}", advisorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
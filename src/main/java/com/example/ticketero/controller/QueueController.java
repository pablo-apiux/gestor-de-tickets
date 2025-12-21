package com.example.ticketero.controller;

import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consulta de estado de colas
 */
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"}, maxAge = 3600)
@Slf4j
public class QueueController {

    private final QueueService queueService;

    /**
     * Obtiene el estado de todas las colas
     */
    @GetMapping
    public ResponseEntity<List<QueueStatusResponse>> obtenerEstadoColas() {
        log.debug("Solicitando estado de todas las colas");
        try {
            List<QueueStatusResponse> estados = queueService.obtenerEstadoColas();
            return ResponseEntity.ok(estados);
        } catch (Exception e) {
            log.error("Error obteniendo estado de colas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el estado de una cola específica
     */
    @GetMapping("/{queueType}")
    public ResponseEntity<QueueStatusResponse> obtenerEstadoCola(@PathVariable QueueType queueType) {
        log.debug("Solicitando estado de cola: {}", queueType);
        try {
            QueueStatusResponse estado = queueService.obtenerEstadoCola(queueType);
            return ResponseEntity.ok(estado);
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de cola inválido: {}", queueType);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error obteniendo estado de cola {}: {}", queueType, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
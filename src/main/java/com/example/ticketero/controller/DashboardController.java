package com.example.ticketero.controller;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para dashboard administrativo
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"}, maxAge = 3600)
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene métricas completas del dashboard
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> obtenerDashboard() {
        log.debug("Solicitando datos del dashboard");
        try {
            DashboardResponse dashboard = dashboardService.obtenerDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error obteniendo dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
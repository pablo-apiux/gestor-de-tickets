package com.example.ticketero.controller;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.service.AdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdvisorController {

    private final AdvisorService advisorService;

    @GetMapping
    public ResponseEntity<List<Advisor>> obtenerTodosLosAsesores() {
        List<Advisor> asesores = advisorService.obtenerTodosLosAsesores();
        return ResponseEntity.ok(asesores);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Advisor>> obtenerAsesoresDisponibles() {
        List<Advisor> asesores = advisorService.obtenerAsesoresDisponibles();
        return ResponseEntity.ok(asesores);
    }

    @PutMapping("/{advisorId}/estado")
    public ResponseEntity<Void> cambiarEstadoAsesor(@PathVariable Long advisorId, @RequestParam AdvisorStatus estado) {
        advisorService.cambiarEstadoAsesor(advisorId, estado);
        return ResponseEntity.ok().build();
    }
}
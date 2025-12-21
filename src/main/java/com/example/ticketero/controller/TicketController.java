package com.example.ticketero.controller;

import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}", maxAge = 3600)
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(@Valid @RequestBody TicketCreateRequest request) {
        try {
            TicketResponse response = ticketService.crearTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> obtenerTicketsActivos() {
        List<TicketResponse> tickets = ticketService.obtenerTicketsActivos();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<TicketResponse> obtenerTicketPorNumero(@PathVariable String numero) {
        try {
            return ticketService.obtenerTicketPorNumero(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{ticketId}/llamar/{advisorId}")
    public ResponseEntity<Void> llamarTicket(@PathVariable Long ticketId, @PathVariable Long advisorId) {
        if (ticketId <= 0 || advisorId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ticketService.llamarTicket(ticketId, advisorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{ticketId}/finalizar")
    public ResponseEntity<Void> finalizarTicket(@PathVariable Long ticketId) {
        if (ticketId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ticketService.finalizarTicket(ticketId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
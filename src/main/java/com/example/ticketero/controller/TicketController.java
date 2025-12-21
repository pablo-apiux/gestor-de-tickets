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
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse response = ticketService.crearTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> obtenerTicketsActivos() {
        List<TicketResponse> tickets = ticketService.obtenerTicketsActivos();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<TicketResponse> obtenerTicketPorNumero(@PathVariable String numero) {
        return ticketService.obtenerTicketPorNumero(numero)
            .map(ticket -> ResponseEntity.ok(ticket))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{ticketId}/llamar/{advisorId}")
    public ResponseEntity<Void> llamarTicket(@PathVariable Long ticketId, @PathVariable Long advisorId) {
        ticketService.llamarTicket(ticketId, advisorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{ticketId}/finalizar")
    public ResponseEntity<Void> finalizarTicket(@PathVariable Long ticketId) {
        ticketService.finalizarTicket(ticketId);
        return ResponseEntity.ok().build();
    }
}
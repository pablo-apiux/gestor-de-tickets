package com.example.ticketero.controller;

import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    @GetMapping
    public ResponseEntity<List<QueueStatusResponse>> obtenerEstadoColas() {
        List<QueueStatusResponse> estados = queueService.obtenerEstadoColas();
        return ResponseEntity.ok(estados);
    }

    @GetMapping("/{queueType}")
    public ResponseEntity<QueueStatusResponse> obtenerEstadoCola(@PathVariable QueueType queueType) {
        QueueStatusResponse estado = queueService.obtenerEstadoCola(queueType);
        return ResponseEntity.ok(estado);
    }
}
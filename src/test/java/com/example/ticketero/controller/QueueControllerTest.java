package com.example.ticketero.controller;

import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.service.QueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueueController.class)
@DisplayName("QueueController")
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueueService queueService;

    @Nested
    @DisplayName("GET /api/queues")
    class ObtenerEstadoColas {

        @Test
        @DisplayName("debe retornar estado de todas las colas")
        void obtenerEstadoColas_debeRetornarEstadoTodasLasColas() throws Exception {
            // Given
            List<QueueStatusResponse> estados = List.of(
                createQueueStatusResponse(QueueType.CAJA, 5, 2),
                createQueueStatusResponse(QueueType.PERSONAL_BANKER, 3, 1)
            );
            when(queueService.obtenerEstadoColas()).thenReturn(estados);

            // When & Then
            mockMvc.perform(get("/api/queues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].queueType").value("CAJA"))
                .andExpect(jsonPath("$[0].waitingTickets").value(5))
                .andExpect(jsonPath("$[1].queueType").value("PERSONAL_BANKER"))
                .andExpect(jsonPath("$[1].waitingTickets").value(3));

            verify(queueService).obtenerEstadoColas();
        }

        @Test
        @DisplayName("sin colas → debe retornar lista vacía")
        void obtenerEstadoColas_sinColas_debeRetornarListaVacia() throws Exception {
            // Given
            when(queueService.obtenerEstadoColas()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/queues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("con excepción → debe retornar 500")
        void obtenerEstadoColas_conExcepcion_debeRetornar500() throws Exception {
            // Given
            when(queueService.obtenerEstadoColas()).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(get("/api/queues"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/queues/{queueType}")
    class ObtenerEstadoCola {

        @Test
        @DisplayName("con tipo válido → debe retornar estado de cola específica")
        void obtenerEstadoCola_conTipoValido_debeRetornarEstadoCola() throws Exception {
            // Given
            QueueStatusResponse estado = createQueueStatusResponse(QueueType.CAJA, 5, 2);
            when(queueService.obtenerEstadoCola(QueueType.CAJA)).thenReturn(estado);

            // When & Then
            mockMvc.perform(get("/api/queues/CAJA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queueType").value("CAJA"))
                .andExpect(jsonPath("$.waitingTickets").value(5))
                .andExpect(jsonPath("$.attendingTickets").value(2))
                .andExpect(jsonPath("$.estimatedWaitTime").value(25));

            verify(queueService).obtenerEstadoCola(QueueType.CAJA);
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void obtenerEstadoCola_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            when(queueService.obtenerEstadoCola(any())).thenThrow(new IllegalArgumentException("Tipo inválido"));

            // When & Then
            mockMvc.perform(get("/api/queues/CAJA"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con excepción genérica → debe retornar 500")
        void obtenerEstadoCola_conExcepcionGenerica_debeRetornar500() throws Exception {
            // Given
            when(queueService.obtenerEstadoCola(any())).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(get("/api/queues/CAJA"))
                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("con todos los tipos de cola → debe funcionar correctamente")
        void obtenerEstadoCola_conTodosLosTipos_debeFuncionarCorrectamente() throws Exception {
            // Given
            when(queueService.obtenerEstadoCola(any())).thenReturn(
                createQueueStatusResponse(QueueType.CAJA, 1, 1)
            );

            // When & Then - CAJA
            mockMvc.perform(get("/api/queues/CAJA"))
                .andExpect(status().isOk());

            // When & Then - PERSONAL_BANKER
            mockMvc.perform(get("/api/queues/PERSONAL_BANKER"))
                .andExpect(status().isOk());

            // When & Then - EMPRESAS
            mockMvc.perform(get("/api/queues/EMPRESAS"))
                .andExpect(status().isOk());

            // When & Then - GERENCIA
            mockMvc.perform(get("/api/queues/GERENCIA"))
                .andExpect(status().isOk());

            verify(queueService).obtenerEstadoCola(QueueType.CAJA);
            verify(queueService).obtenerEstadoCola(QueueType.PERSONAL_BANKER);
            verify(queueService).obtenerEstadoCola(QueueType.EMPRESAS);
            verify(queueService).obtenerEstadoCola(QueueType.GERENCIA);
        }
    }

    private QueueStatusResponse createQueueStatusResponse(QueueType queueType, int waiting, int attending) {
        return new QueueStatusResponse(
            queueType,
            queueType.getDisplayName(),
            queueType.getAvgTimeMinutes(),
            queueType.getPriority(),
            String.valueOf(queueType.getPrefix()),
            waiting + attending,
            waiting,
            attending,
            waiting * queueType.getAvgTimeMinutes(),
            java.time.LocalDateTime.now(),
            List.of()
        );
    }
}
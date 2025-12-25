package com.example.ticketero.controller;

import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@DisplayName("TicketController")
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Nested
    @DisplayName("POST /api/tickets")
    class CrearTicket {

        @Test
        @DisplayName("con datos válidos → debe crear ticket y retornar 201")
        void crearTicket_conDatosValidos_debeRetornar201() throws Exception {
            // Given
            TicketCreateRequest request = new TicketCreateRequest(
                "12345678-9", "+56912345678", "Sucursal Centro", QueueType.CAJA
            );
            TicketResponse response = new TicketResponse(
                UUID.randomUUID(), "C001", "12345678-9", "+56912345678", "Sucursal Centro",
                QueueType.CAJA, TicketStatus.EN_ESPERA, 1, 5, null, null,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(ticketService.crearTicket(any(TicketCreateRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("C001"))
                .andExpect(jsonPath("$.nationalId").value("12345678-9"))
                .andExpect(jsonPath("$.status").value("EN_ESPERA"));

            verify(ticketService).crearTicket(any(TicketCreateRequest.class));
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void crearTicket_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            TicketCreateRequest request = new TicketCreateRequest(
                "12345678-9", "+56912345678", "Sucursal Centro", QueueType.CAJA
            );

            when(ticketService.crearTicket(any())).thenThrow(new IllegalArgumentException("Error"));

            // When & Then
            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con IllegalStateException → debe retornar 400")
        void crearTicket_conIllegalStateException_debeRetornar400() throws Exception {
            // Given
            TicketCreateRequest request = new TicketCreateRequest(
                "12345678-9", "+56912345678", "Sucursal Centro", QueueType.CAJA
            );

            when(ticketService.crearTicket(any())).thenThrow(new IllegalStateException("Error"));

            // When & Then
            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con excepción genérica → debe retornar 500")
        void crearTicket_conExcepcionGenerica_debeRetornar500() throws Exception {
            // Given
            TicketCreateRequest request = new TicketCreateRequest(
                "12345678-9", "+56912345678", "Sucursal Centro", QueueType.CAJA
            );

            when(ticketService.crearTicket(any())).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/tickets")
    class ObtenerTicketsActivos {

        @Test
        @DisplayName("debe retornar lista de tickets activos")
        void obtenerTicketsActivos_debeRetornarListaTickets() throws Exception {
            // Given
            List<TicketResponse> tickets = List.of(
                new TicketResponse(UUID.randomUUID(), "C001", "12345678-9", "+56912345678", "Centro",
                    QueueType.CAJA, TicketStatus.EN_ESPERA, 1, 5, null, null,
                    LocalDateTime.now(), LocalDateTime.now()),
                new TicketResponse(UUID.randomUUID(), "C002", "87654321-0", "+56987654321", "Centro",
                    QueueType.PERSONAL_BANKER, TicketStatus.EN_ESPERA, 2, 10, null, null,
                    LocalDateTime.now(), LocalDateTime.now())
            );

            when(ticketService.obtenerTicketsActivos()).thenReturn(tickets);

            // When & Then
            mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numero").value("C001"))
                .andExpect(jsonPath("$[1].numero").value("C002"));

            verify(ticketService).obtenerTicketsActivos();
        }

        @Test
        @DisplayName("sin tickets activos → debe retornar lista vacía")
        void obtenerTicketsActivos_sinTickets_debeRetornarListaVacia() throws Exception {
            // Given
            when(ticketService.obtenerTicketsActivos()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/tickets/{numero}")
    class ObtenerTicketPorNumero {

        @Test
        @DisplayName("con ticket existente → debe retornar ticket")
        void obtenerTicketPorNumero_conTicketExistente_debeRetornarTicket() throws Exception {
            // Given
            TicketResponse ticket = new TicketResponse(
                UUID.randomUUID(), "C001", "12345678-9", "+56912345678", "Centro",
                QueueType.CAJA, TicketStatus.EN_ESPERA, 1, 5, null, null,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(ticketService.obtenerTicketPorNumero("C001")).thenReturn(Optional.of(ticket));

            // When & Then
            mockMvc.perform(get("/api/tickets/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("C001"))
                .andExpect(jsonPath("$.nationalId").value("12345678-9"));

            verify(ticketService).obtenerTicketPorNumero("C001");
        }

        @Test
        @DisplayName("con ticket inexistente → debe retornar 404")
        void obtenerTicketPorNumero_conTicketInexistente_debeRetornar404() throws Exception {
            // Given
            when(ticketService.obtenerTicketPorNumero("C999")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/tickets/C999"))
                .andExpect(status().isNotFound());

            verify(ticketService).obtenerTicketPorNumero("C999");
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void obtenerTicketPorNumero_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            when(ticketService.obtenerTicketPorNumero(anyString()))
                .thenThrow(new IllegalArgumentException("Número inválido"));

            // When & Then
            mockMvc.perform(get("/api/tickets/INVALID"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/tickets/{ticketId}/llamar/{advisorId}")
    class LlamarTicket {

        @Test
        @DisplayName("con IDs válidos → debe llamar ticket y retornar 200")
        void llamarTicket_conIdsValidos_debeRetornar200() throws Exception {
            // Given
            doNothing().when(ticketService).llamarTicket(1L, 2L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/llamar/2"))
                .andExpect(status().isOk());

            verify(ticketService).llamarTicket(1L, 2L);
        }

        @Test
        @DisplayName("con ID de ticket inválido → debe retornar 400")
        void llamarTicket_conTicketIdInvalido_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/tickets/0/llamar/2"))
                .andExpect(status().isBadRequest());

            verify(ticketService, never()).llamarTicket(anyLong(), anyLong());
        }

        @Test
        @DisplayName("con ID de advisor inválido → debe retornar 400")
        void llamarTicket_conAdvisorIdInvalido_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/tickets/1/llamar/0"))
                .andExpect(status().isBadRequest());

            verify(ticketService, never()).llamarTicket(anyLong(), anyLong());
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void llamarTicket_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Error")).when(ticketService).llamarTicket(1L, 2L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/llamar/2"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con IllegalStateException → debe retornar 400")
        void llamarTicket_conIllegalStateException_debeRetornar400() throws Exception {
            // Given
            doThrow(new IllegalStateException("Error")).when(ticketService).llamarTicket(1L, 2L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/llamar/2"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/tickets/{ticketId}/finalizar")
    class FinalizarTicket {

        @Test
        @DisplayName("con ID válido → debe finalizar ticket y retornar 200")
        void finalizarTicket_conIdValido_debeRetornar200() throws Exception {
            // Given
            doNothing().when(ticketService).finalizarTicket(1L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/finalizar"))
                .andExpect(status().isOk());

            verify(ticketService).finalizarTicket(1L);
        }

        @Test
        @DisplayName("con ID inválido → debe retornar 400")
        void finalizarTicket_conIdInvalido_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/tickets/0/finalizar"))
                .andExpect(status().isBadRequest());

            verify(ticketService, never()).finalizarTicket(anyLong());
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void finalizarTicket_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Error")).when(ticketService).finalizarTicket(1L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/finalizar"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con IllegalStateException → debe retornar 400")
        void finalizarTicket_conIllegalStateException_debeRetornar400() throws Exception {
            // Given
            doThrow(new IllegalStateException("Error")).when(ticketService).finalizarTicket(1L);

            // When & Then
            mockMvc.perform(put("/api/tickets/1/finalizar"))
                .andExpect(status().isBadRequest());
        }
    }
}
package com.example.ticketero.controller;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Nested
    @DisplayName("GET /api/admin/dashboard")
    class ObtenerDashboard {

        @Test
        @DisplayName("debe retornar dashboard completo")
        void obtenerDashboard_debeRetornarDashboardCompleto() throws Exception {
            // Given
            DashboardResponse dashboard = createDashboardResponse();
            when(dashboardService.obtenerDashboard()).thenReturn(dashboard);

            // When & Then
            mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary.totalTicketsToday").value(25))
                .andExpect(jsonPath("$.summary.waitingTickets").value(8))
                .andExpect(jsonPath("$.advisors").exists())
                .andExpect(jsonPath("$.advisors.available").value(3))
                .andExpect(jsonPath("$.queues").exists())
                .andExpect(jsonPath("$.queues.length()").value(4))
                .andExpect(jsonPath("$.alerts").exists());

            verify(dashboardService).obtenerDashboard();
        }

        @Test
        @DisplayName("con excepción → debe retornar 500")
        void obtenerDashboard_conExcepcion_debeRetornar500() throws Exception {
            // Given
            when(dashboardService.obtenerDashboard()).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isInternalServerError());

            verify(dashboardService).obtenerDashboard();
        }

        @Test
        @DisplayName("debe manejar dashboard vacío")
        void obtenerDashboard_debeManejarDashboardVacio() throws Exception {
            // Given
            DashboardResponse dashboard = createEmptyDashboardResponse();
            when(dashboardService.obtenerDashboard()).thenReturn(dashboard);

            // When & Then
            mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTicketsToday").value(0))
                .andExpect(jsonPath("$.advisors.totalCapacity").value(0))
                .andExpect(jsonPath("$.queues.length()").value(0))
                .andExpect(jsonPath("$.alerts.length()").value(0));
        }

        private DashboardResponse createDashboardResponse() {
            return new DashboardResponse(
                LocalDateTime.now(),
                new DashboardResponse.SummaryData(25, 8, 5, 12, 15, "10:00-11:00"),
                new DashboardResponse.AdvisorData(3, 2, 1, 6),
                List.of(
                    new DashboardResponse.QueueData("CAJA", "Caja", 4, 2, 6, 20, 5, "NORMAL"),
                    new DashboardResponse.QueueData("PERSONAL_BANKER", "Personal Banker", 2, 1, 3, 30, 15, "NORMAL"),
                    new DashboardResponse.QueueData("EMPRESAS", "Empresas", 1, 1, 2, 20, 20, "NORMAL"),
                    new DashboardResponse.QueueData("GERENCIA", "Gerencia", 1, 1, 1, 30, 30, "NORMAL")
                ),
                List.of()
            );
        }

        private DashboardResponse createEmptyDashboardResponse() {
            return new DashboardResponse(
                LocalDateTime.now(),
                new DashboardResponse.SummaryData(0, 0, 0, 0, 0, "N/A"),
                new DashboardResponse.AdvisorData(0, 0, 0, 0),
                List.of(),
                List.of()
            );
        }
    }
}
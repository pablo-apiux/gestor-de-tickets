package com.example.ticketero.controller;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.service.AdvisorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvisorController.class)
@DisplayName("AdvisorController")
class AdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvisorService advisorService;

    @Nested
    @DisplayName("GET /api/advisors")
    class ObtenerTodosLosAsesores {

        @Test
        @DisplayName("debe retornar lista de asesores")
        void obtenerTodosLosAsesores_debeRetornarListaAsesores() throws Exception {
            // Given
            List<Advisor> asesores = List.of(
                createAdvisor(1L, "María López", AdvisorStatus.AVAILABLE),
                createAdvisor(2L, "Juan Pérez", AdvisorStatus.BUSY)
            );
            when(advisorService.obtenerTodosLosAsesores()).thenReturn(asesores);

            // When & Then
            mockMvc.perform(get("/api/advisors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("María López"))
                .andExpect(jsonPath("$[1].name").value("Juan Pérez"));

            verify(advisorService).obtenerTodosLosAsesores();
        }

        @Test
        @DisplayName("sin asesores → debe retornar lista vacía")
        void obtenerTodosLosAsesores_sinAsesores_debeRetornarListaVacia() throws Exception {
            // Given
            when(advisorService.obtenerTodosLosAsesores()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/advisors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("con excepción → debe retornar 500")
        void obtenerTodosLosAsesores_conExcepcion_debeRetornar500() throws Exception {
            // Given
            when(advisorService.obtenerTodosLosAsesores()).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(get("/api/advisors"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/advisors/disponibles")
    class ObtenerAsesoresDisponibles {

        @Test
        @DisplayName("debe retornar solo asesores disponibles")
        void obtenerAsesoresDisponibles_debeRetornarSoloDisponibles() throws Exception {
            // Given
            List<Advisor> asesoresDisponibles = List.of(
                createAdvisor(1L, "María López", AdvisorStatus.AVAILABLE),
                createAdvisor(3L, "Ana García", AdvisorStatus.AVAILABLE)
            );
            when(advisorService.obtenerAsesoresDisponibles()).thenReturn(asesoresDisponibles);

            // When & Then
            mockMvc.perform(get("/api/advisors/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].status").value("AVAILABLE"));

            verify(advisorService).obtenerAsesoresDisponibles();
        }

        @Test
        @DisplayName("sin asesores disponibles → debe retornar lista vacía")
        void obtenerAsesoresDisponibles_sinDisponibles_debeRetornarListaVacia() throws Exception {
            // Given
            when(advisorService.obtenerAsesoresDisponibles()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/advisors/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("con excepción → debe retornar 500")
        void obtenerAsesoresDisponibles_conExcepcion_debeRetornar500() throws Exception {
            // Given
            when(advisorService.obtenerAsesoresDisponibles()).thenThrow(new RuntimeException("Error interno"));

            // When & Then
            mockMvc.perform(get("/api/advisors/disponibles"))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PUT /api/advisors/{advisorId}/estado")
    class CambiarEstadoAsesor {

        @Test
        @DisplayName("con datos válidos → debe cambiar estado y retornar 200")
        void cambiarEstadoAsesor_conDatosValidos_debeRetornar200() throws Exception {
            // Given
            doNothing().when(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.BUSY);

            // When & Then
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "BUSY"))
                .andExpect(status().isOk());

            verify(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.BUSY);
        }

        @Test
        @DisplayName("con ID inválido → debe retornar 400")
        void cambiarEstadoAsesor_conIdInvalido_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/advisors/0/estado")
                    .param("estado", "BUSY"))
                .andExpect(status().isBadRequest());

            verify(advisorService, never()).cambiarEstadoAsesor(anyLong(), any());
        }

        @Test
        @DisplayName("sin parámetro estado → debe retornar 400")
        void cambiarEstadoAsesor_sinParametroEstado_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/advisors/1/estado"))
                .andExpect(status().isBadRequest());

            verify(advisorService, never()).cambiarEstadoAsesor(anyLong(), any());
        }

        @Test
        @DisplayName("con IllegalArgumentException → debe retornar 400")
        void cambiarEstadoAsesor_conIllegalArgumentException_debeRetornar400() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Asesor no encontrado"))
                .when(advisorService).cambiarEstadoAsesor(999L, AdvisorStatus.BUSY);

            // When & Then
            mockMvc.perform(put("/api/advisors/999/estado")
                    .param("estado", "BUSY"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("con excepción genérica → debe retornar 500")
        void cambiarEstadoAsesor_conExcepcionGenerica_debeRetornar500() throws Exception {
            // Given
            doThrow(new RuntimeException("Error interno"))
                .when(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.BUSY);

            // When & Then
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "BUSY"))
                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("con todos los estados válidos → debe funcionar correctamente")
        void cambiarEstadoAsesor_conTodosLosEstados_debeFuncionarCorrectamente() throws Exception {
            // Given
            doNothing().when(advisorService).cambiarEstadoAsesor(anyLong(), any());

            // When & Then - AVAILABLE
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "AVAILABLE"))
                .andExpect(status().isOk());

            // When & Then - BUSY
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "BUSY"))
                .andExpect(status().isOk());

            // When & Then - OFFLINE
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "OFFLINE"))
                .andExpect(status().isOk());

            verify(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.AVAILABLE);
            verify(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.BUSY);
            verify(advisorService).cambiarEstadoAsesor(1L, AdvisorStatus.OFFLINE);
        }

        @Test
        @DisplayName("con estado inválido → debe retornar 400")
        void cambiarEstadoAsesor_conEstadoInvalido_debeRetornar400() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/advisors/1/estado")
                    .param("estado", "ESTADO_INEXISTENTE"))
                .andExpect(status().isBadRequest());

            verify(advisorService, never()).cambiarEstadoAsesor(anyLong(), any());
        }
    }

    private Advisor createAdvisor(Long id, String name, AdvisorStatus status) {
        return Advisor.builder()
            .id(id)
            .name(name)
            .email(name.toLowerCase().replace(" ", ".") + "@banco.com")
            .moduleNumber(id.intValue())
            .status(status)
            .assignedTicketsCount(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.repository.AdvisorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.ticketero.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdvisorService - Unit Tests")
class AdvisorServiceTest {

    @Mock
    private AdvisorRepository advisorRepository;

    @InjectMocks
    private AdvisorService advisorService;

    // ============================================================
    // OBTENER ASESORES DISPONIBLES
    // ============================================================
    
    @Nested
    @DisplayName("obtenerAsesoresDisponibles()")
    class ObtenerAsesoresDisponibles {

        @Test
        @DisplayName("con asesores disponibles → debe retornar lista ordenada por carga")
        void obtenerAsesores_conDisponibles_debeRetornarOrdenados() {
            // Given
            Advisor advisor1 = advisorAvailable().id(1L).assignedTicketsCount(5).build();
            Advisor advisor2 = advisorAvailable().id(2L).assignedTicketsCount(2).build();
            List<Advisor> asesores = List.of(advisor2, advisor1); // Ya ordenados por el repo

            when(advisorRepository.findByStatusOrderByAssignedTicketsCountAscUpdatedAtAsc(AdvisorStatus.AVAILABLE))
                .thenReturn(asesores);

            // When
            List<Advisor> resultado = advisorService.obtenerAsesoresDisponibles();

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getAssignedTicketsCount()).isEqualTo(2);
            assertThat(resultado.get(1).getAssignedTicketsCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("sin asesores disponibles → debe retornar lista vacía")
        void obtenerAsesores_sinDisponibles_debeRetornarVacia() {
            // Given
            when(advisorRepository.findByStatusOrderByAssignedTicketsCountAscUpdatedAtAsc(AdvisorStatus.AVAILABLE))
                .thenReturn(Collections.emptyList());

            // When
            List<Advisor> resultado = advisorService.obtenerAsesoresDisponibles();

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    // ============================================================
    // OBTENER ASESOR CON MENOS TICKETS
    // ============================================================
    
    @Nested
    @DisplayName("obtenerAsesorConMenosTickets()")
    class ObtenerAsesorConMenosTickets {

        @Test
        @DisplayName("con asesor disponible → debe retornar el de menor carga")
        void obtenerAsesor_conDisponible_debeRetornarMenorCarga() {
            // Given
            Advisor advisor = advisorAvailable().assignedTicketsCount(3).build();
            when(advisorRepository.findFirstByStatusOrderByAssignedTicketsCountAsc(AdvisorStatus.AVAILABLE))
                .thenReturn(Optional.of(advisor));

            // When
            Optional<Advisor> resultado = advisorService.obtenerAsesorConMenosTickets();

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getAssignedTicketsCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("sin asesores disponibles → debe retornar Optional.empty()")
        void obtenerAsesor_sinDisponibles_debeRetornarEmpty() {
            // Given
            when(advisorRepository.findFirstByStatusOrderByAssignedTicketsCountAsc(AdvisorStatus.AVAILABLE))
                .thenReturn(Optional.empty());

            // When
            Optional<Advisor> resultado = advisorService.obtenerAsesorConMenosTickets();

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    // ============================================================
    // CAMBIAR ESTADO ASESOR
    // ============================================================
    
    @Nested
    @DisplayName("cambiarEstadoAsesor()")
    class CambiarEstadoAsesor {

        @Test
        @DisplayName("con datos válidos → debe cambiar estado correctamente")
        void cambiarEstado_conDatosValidos_debeCambiarEstado() {
            // Given
            Advisor advisor = advisorAvailable().build();
            when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));

            // When
            advisorService.cambiarEstadoAsesor(1L, AdvisorStatus.BUSY);

            // Then
            assertThat(advisor.getStatus()).isEqualTo(AdvisorStatus.BUSY);
            verify(advisorRepository).save(advisor);
        }

        @Test
        @DisplayName("con ID null → debe lanzar IllegalArgumentException")
        void cambiarEstado_conIdNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> advisorService.cambiarEstadoAsesor(null, AdvisorStatus.BUSY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID del asesor y nuevo estado no pueden ser null");
        }

        @Test
        @DisplayName("con estado null → debe lanzar IllegalArgumentException")
        void cambiarEstado_conEstadoNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> advisorService.cambiarEstadoAsesor(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID del asesor y nuevo estado no pueden ser null");
        }

        @Test
        @DisplayName("con asesor inexistente → debe lanzar IllegalArgumentException")
        void cambiarEstado_conAsesorInexistente_debeLanzarExcepcion() {
            // Given
            when(advisorRepository.findById(999L)).thenReturn(Optional.empty());

            // When + Then
            assertThatThrownBy(() -> advisorService.cambiarEstadoAsesor(999L, AdvisorStatus.BUSY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Asesor no encontrado con ID: 999");
        }
    }

    // ============================================================
    // INCREMENTAR CONTADOR TICKETS
    // ============================================================
    
    @Nested
    @DisplayName("incrementarContadorTickets()")
    class IncrementarContadorTickets {

        @Test
        @DisplayName("con asesor válido → debe incrementar contador")
        void incrementarContador_conAsesorValido_debeIncrementar() {
            // Given
            Advisor advisor = advisorAvailable().assignedTicketsCount(5).build();
            when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));

            // When
            advisorService.incrementarContadorTickets(1L);

            // Then
            assertThat(advisor.getAssignedTicketsCount()).isEqualTo(6);
            verify(advisorRepository).save(advisor);
        }

        @Test
        @DisplayName("con ID null → debe lanzar IllegalArgumentException")
        void incrementarContador_conIdNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> advisorService.incrementarContadorTickets(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID del asesor no puede ser null");
        }

        @Test
        @DisplayName("con asesor inexistente → debe lanzar IllegalArgumentException")
        void incrementarContador_conAsesorInexistente_debeLanzarExcepcion() {
            // Given
            when(advisorRepository.findById(999L)).thenReturn(Optional.empty());

            // When + Then
            assertThatThrownBy(() -> advisorService.incrementarContadorTickets(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Asesor no encontrado con ID: 999");
        }
    }

    // ============================================================
    // OBTENER TODOS LOS ASESORES
    // ============================================================
    
    @Nested
    @DisplayName("obtenerTodosLosAsesores()")
    class ObtenerTodosLosAsesores {

        @Test
        @DisplayName("debe retornar todos los asesores del sistema")
        void obtenerTodos_debeRetornarTodos() {
            // Given
            List<Advisor> asesores = List.of(
                advisorAvailable().id(1L).build(),
                advisorBusy().id(2L).build(),
                advisorAvailable().id(3L).status(AdvisorStatus.OFFLINE).build()
            );
            when(advisorRepository.findAll()).thenReturn(asesores);

            // When
            List<Advisor> resultado = advisorService.obtenerTodosLosAsesores();

            // Then
            assertThat(resultado).hasSize(3);
            assertThat(resultado).containsExactlyElementsOf(asesores);
        }

        @Test
        @DisplayName("sin asesores en el sistema → debe retornar lista vacía")
        void obtenerTodos_sinAsesores_debeRetornarVacia() {
            // Given
            when(advisorRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<Advisor> resultado = advisorService.obtenerTodosLosAsesores();

            // Then
            assertThat(resultado).isEmpty();
        }
    }
}
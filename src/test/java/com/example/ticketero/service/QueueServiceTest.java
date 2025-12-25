package com.example.ticketero.service;

import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.example.ticketero.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueueService - Unit Tests")
class QueueServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private QueueService queueService;

    // ============================================================
    // OBTENER ESTADO COLAS
    // ============================================================
    
    @Nested
    @DisplayName("obtenerEstadoColas()")
    class ObtenerEstadoColas {

        @Test
        @DisplayName("debe retornar estado de todas las colas")
        void obtenerEstadoColas_debeRetornarTodasLasColas() {
            // Given
            when(ticketRepository.countByQueueTypeAndStatus(any(QueueType.class), eq(TicketStatus.EN_ESPERA)))
                .thenReturn(2L);
            when(ticketRepository.countByQueueTypeAndStatus(any(QueueType.class), eq(TicketStatus.ATENDIENDO)))
                .thenReturn(1L);
            when(ticketRepository.findNextTicketsForQueue(any(QueueType.class), any()))
                .thenReturn(Collections.emptyList());

            // When
            List<QueueStatusResponse> resultado = queueService.obtenerEstadoColas();

            // Then
            assertThat(resultado).hasSize(4); // CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA
            assertThat(resultado).extracting(QueueStatusResponse::queueType)
                .containsExactlyInAnyOrder(
                    QueueType.CAJA, 
                    QueueType.PERSONAL_BANKER, 
                    QueueType.EMPRESAS, 
                    QueueType.GERENCIA
                );
        }

        @Test
        @DisplayName("debe calcular estadísticas correctamente")
        void obtenerEstadoColas_debeCalcularEstadisticas() {
            // Given
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.EN_ESPERA))
                .thenReturn(3L);
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.ATENDIENDO))
                .thenReturn(2L);
            when(ticketRepository.findNextTicketsForQueue(eq(QueueType.CAJA), any()))
                .thenReturn(Collections.emptyList());
            
            // Mock para otras colas
            when(ticketRepository.countByQueueTypeAndStatus(argThat(qt -> qt != QueueType.CAJA), any()))
                .thenReturn(0L);
            when(ticketRepository.findNextTicketsForQueue(argThat(qt -> qt != QueueType.CAJA), any()))
                .thenReturn(Collections.emptyList());

            // When
            List<QueueStatusResponse> resultado = queueService.obtenerEstadoColas();

            // Then
            QueueStatusResponse cajaCola = resultado.stream()
                .filter(q -> q.queueType() == QueueType.CAJA)
                .findFirst()
                .orElseThrow();

            assertThat(cajaCola.totalTickets()).isEqualTo(5); // 3 + 2
            assertThat(cajaCola.waitingTickets()).isEqualTo(3);
            assertThat(cajaCola.attendingTickets()).isEqualTo(2);
            assertThat(cajaCola.estimatedWaitTime()).isEqualTo(15); // 3 * 5 min
        }
    }

    // ============================================================
    // OBTENER ESTADO COLA ESPECÍFICA
    // ============================================================
    
    @Nested
    @DisplayName("obtenerEstadoCola()")
    class ObtenerEstadoCola {

        @Test
        @DisplayName("con cola válida → debe retornar estado correcto")
        void obtenerEstadoCola_conColaValida_debeRetornarEstado() {
            // Given
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.EN_ESPERA))
                .thenReturn(4L);
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.ATENDIENDO))
                .thenReturn(1L);
            when(ticketRepository.findNextTicketsForQueue(eq(QueueType.CAJA), any()))
                .thenReturn(Collections.emptyList());

            // When
            QueueStatusResponse resultado = queueService.obtenerEstadoCola(QueueType.CAJA);

            // Then
            assertThat(resultado.queueType()).isEqualTo(QueueType.CAJA);
            assertThat(resultado.displayName()).isEqualTo("Caja");
            assertThat(resultado.totalTickets()).isEqualTo(5);
            assertThat(resultado.waitingTickets()).isEqualTo(4);
            assertThat(resultado.attendingTickets()).isEqualTo(1);
            assertThat(resultado.estimatedWaitTime()).isEqualTo(20); // 4 * 5 min
        }

        @Test
        @DisplayName("con cola null → debe lanzar IllegalArgumentException")
        void obtenerEstadoCola_conColaNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> queueService.obtenerEstadoCola(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de cola no puede ser null");
        }
    }

    // ============================================================
    // OBTENER PRÓXIMOS TICKETS
    // ============================================================
    
    @Nested
    @DisplayName("obtenerProximosTickets()")
    class ObtenerProximosTickets {

        @Test
        @DisplayName("con tickets disponibles → debe retornar lista limitada")
        void obtenerProximosTickets_conTicketsDisponibles_debeRetornarLimitada() {
            // Given
            List<Ticket> tickets = List.of(
                ticketWaiting().id(1L).numero("C001").positionInQueue(1).build(),
                ticketWaiting().id(2L).numero("C002").positionInQueue(2).build(),
                ticketWaiting().id(3L).numero("C003").positionInQueue(3).build()
            );
            when(ticketRepository.findNextTicketsForQueue(eq(QueueType.CAJA), any()))
                .thenReturn(tickets);

            // When
            List<Ticket> resultado = queueService.obtenerProximosTickets(QueueType.CAJA, 2);

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getNumero()).isEqualTo("C001");
            assertThat(resultado.get(1).getNumero()).isEqualTo("C002");
        }

        @Test
        @DisplayName("sin tickets → debe retornar lista vacía")
        void obtenerProximosTickets_sinTickets_debeRetornarVacia() {
            // Given
            when(ticketRepository.findNextTicketsForQueue(eq(QueueType.CAJA), any()))
                .thenReturn(Collections.emptyList());

            // When
            List<Ticket> resultado = queueService.obtenerProximosTickets(QueueType.CAJA, 5);

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("con cola null → debe lanzar IllegalArgumentException")
        void obtenerProximosTickets_conColaNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> queueService.obtenerProximosTickets(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de cola no puede ser null");
        }

        @Test
        @DisplayName("con límite inválido → debe lanzar IllegalArgumentException")
        void obtenerProximosTickets_conLimiteInvalido_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> queueService.obtenerProximosTickets(QueueType.CAJA, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Límite debe ser mayor a 0");

            assertThatThrownBy(() -> queueService.obtenerProximosTickets(QueueType.CAJA, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Límite debe ser mayor a 0");
        }

        @Test
        @DisplayName("debe respetar el límite cuando hay más tickets")
        void obtenerProximosTickets_debeRespetarLimite() {
            // Given
            List<Ticket> muchos_tickets = List.of(
                ticketWaiting().id(1L).build(),
                ticketWaiting().id(2L).build(),
                ticketWaiting().id(3L).build(),
                ticketWaiting().id(4L).build(),
                ticketWaiting().id(5L).build()
            );
            when(ticketRepository.findNextTicketsForQueue(eq(QueueType.CAJA), any()))
                .thenReturn(muchos_tickets);

            // When
            List<Ticket> resultado = queueService.obtenerProximosTickets(QueueType.CAJA, 3);

            // Then
            assertThat(resultado).hasSize(3);
        }
    }
}
package com.example.ticketero.service;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AdvisorRepository advisorRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Nested
    @DisplayName("obtenerDashboard")
    class ObtenerDashboard {

        @Test
        @DisplayName("debe generar dashboard completo con métricas")
        void debeGenerarDashboardCompleto() {
            // Given
            mockearRepositorios();

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            assertThat(dashboard).isNotNull();
            assertThat(dashboard.timestamp()).isNotNull();
            assertThat(dashboard.summary()).isNotNull();
            assertThat(dashboard.advisors()).isNotNull();
            assertThat(dashboard.queues()).hasSize(QueueType.values().length);
            assertThat(dashboard.alerts()).isNotNull();
        }

        @Test
        @DisplayName("debe generar summary data correctamente")
        void debeGenerarSummaryDataCorrectamente() {
            // Given
            mockearRepositorios();

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            DashboardResponse.SummaryData summary = dashboard.summary();
            assertThat(summary.totalTicketsToday()).isEqualTo(25);
            assertThat(summary.waitingTickets()).isEqualTo(8);
            assertThat(summary.attendingTickets()).isEqualTo(5);
            assertThat(summary.completedTickets()).isEqualTo(12);
            assertThat(summary.averageWaitTimeMinutes()).isEqualTo(15);
            assertThat(summary.peakHour()).isEqualTo("10:00-11:00");
        }

        @Test
        @DisplayName("debe generar advisor data correctamente")
        void debeGenerarAdvisorDataCorrectamente() {
            // Given
            mockearRepositorios();

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            DashboardResponse.AdvisorData advisors = dashboard.advisors();
            assertThat(advisors.available()).isEqualTo(3);
            assertThat(advisors.busy()).isEqualTo(2);
            assertThat(advisors.offline()).isEqualTo(1);
            assertThat(advisors.totalCapacity()).isEqualTo(6);
        }

        @Test
        @DisplayName("debe generar queue data para todos los tipos")
        void debeGenerarQueueDataParaTodosLosTipos() {
            // Given
            mockearRepositorios();

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            assertThat(dashboard.queues()).hasSize(QueueType.values().length);
            
            DashboardResponse.QueueData cajaQueue = dashboard.queues().stream()
                .filter(q -> q.queueType().equals("CAJA"))
                .findFirst()
                .orElseThrow();
                
            assertThat(cajaQueue.waitingTickets()).isEqualTo(4);
            assertThat(cajaQueue.attendingTickets()).isEqualTo(2);
            assertThat(cajaQueue.completedToday()).isEqualTo(6);
        }

        @Test
        @DisplayName("debe calcular estado de cola según demanda")
        void debeCalcularEstadoColaSegunDemanda() {
            // Given
            when(ticketRepository.countByQueueTypeAndStatus(any(), eq(TicketStatus.EN_ESPERA)))
                .thenReturn(15L); // Alta demanda
            mockearOtrosRepositorios();

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            DashboardResponse.QueueData queue = dashboard.queues().get(0);
            assertThat(queue.status()).isEqualTo("ALTA_DEMANDA");
        }

        @Test
        @DisplayName("debe manejar valores cero correctamente")
        void debeManejarValoresCeroCorrectamente() {
            // Given
            when(ticketRepository.countByCreatedAtAfter(any())).thenReturn(0L);
            when(ticketRepository.countByStatus(any())).thenReturn(0L);
            when(ticketRepository.countByStatusAndCreatedAtAfter(any(), any())).thenReturn(0L);
            when(advisorRepository.countByStatus(any())).thenReturn(0L);
            when(advisorRepository.count()).thenReturn(0L);
            when(ticketRepository.countByQueueTypeAndStatus(any(), any())).thenReturn(0L);
            when(ticketRepository.countByQueueTypeAndStatusAndCreatedAtAfter(any(), any(), any())).thenReturn(0L);

            // When
            DashboardResponse dashboard = dashboardService.obtenerDashboard();

            // Then
            assertThat(dashboard.summary().totalTicketsToday()).isZero();
            assertThat(dashboard.advisors().totalCapacity()).isZero();
            assertThat(dashboard.queues().get(0).waitingTickets()).isZero();
        }

        private void mockearRepositorios() {
            // Summary data mocks
            when(ticketRepository.countByCreatedAtAfter(any())).thenReturn(25L);
            when(ticketRepository.countByStatus(TicketStatus.EN_ESPERA)).thenReturn(8L);
            when(ticketRepository.countByStatus(TicketStatus.ATENDIENDO)).thenReturn(5L);
            when(ticketRepository.countByStatusAndCreatedAtAfter(eq(TicketStatus.COMPLETADO), any())).thenReturn(12L);

            // Advisor data mocks
            when(advisorRepository.countByStatus(AdvisorStatus.AVAILABLE)).thenReturn(3L);
            when(advisorRepository.countByStatus(AdvisorStatus.BUSY)).thenReturn(2L);
            when(advisorRepository.countByStatus(AdvisorStatus.OFFLINE)).thenReturn(1L);
            when(advisorRepository.count()).thenReturn(6L);

            // Queue data mocks
            when(ticketRepository.countByQueueTypeAndStatus(any(), eq(TicketStatus.EN_ESPERA))).thenReturn(4L);
            when(ticketRepository.countByQueueTypeAndStatus(any(), eq(TicketStatus.ATENDIENDO))).thenReturn(2L);
            when(ticketRepository.countByQueueTypeAndStatusAndCreatedAtAfter(any(), eq(TicketStatus.COMPLETADO), any())).thenReturn(6L);
        }

        private void mockearOtrosRepositorios() {
            when(ticketRepository.countByCreatedAtAfter(any())).thenReturn(25L);
            when(ticketRepository.countByStatus(any())).thenReturn(5L);
            when(ticketRepository.countByStatusAndCreatedAtAfter(any(), any())).thenReturn(12L);
            when(advisorRepository.countByStatus(any())).thenReturn(2L);
            when(advisorRepository.count()).thenReturn(6L);
            when(ticketRepository.countByQueueTypeAndStatus(any(), eq(TicketStatus.ATENDIENDO))).thenReturn(2L);
            when(ticketRepository.countByQueueTypeAndStatusAndCreatedAtAfter(any(), any(), any())).thenReturn(6L);
        }
    }
}
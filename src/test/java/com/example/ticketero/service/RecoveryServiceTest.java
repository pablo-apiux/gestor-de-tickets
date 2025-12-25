package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.entity.RecoveryEvent;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.RecoveryEventRepository;
import com.example.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.ticketero.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecoveryService - Unit Tests")
class RecoveryServiceTest {

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RecoveryEventRepository recoveryEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RecoveryService recoveryService;

    @Nested
    @DisplayName("detectarYRecuperarWorkersMuertos()")
    class DetectarWorkersMuertos {

        @Test
        @DisplayName("con worker muerto → debe liberar advisor y reencolar ticket")
        void detectar_conWorkerMuerto_debeRecuperar() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "heartbeatTimeoutSeconds", 60);
            ReflectionTestUtils.setField(recoveryService, "exchangeName", "ticketero-exchange");

            Ticket ticketEnProgreso = ticketInProgress().build();
            Advisor advisorMuerto = advisorBusy().build();

            when(advisorRepository.findDeadWorkers(any(LocalDateTime.class)))
                .thenReturn(List.of(advisorMuerto));
            when(ticketRepository.findCurrentTicketForAdvisor(any()))
                .thenReturn(Optional.of(ticketEnProgreso));

            // When
            recoveryService.detectarYRecuperarWorkersMuertos();

            // Then
            assertThat(advisorMuerto.getStatus()).isEqualTo(AdvisorStatus.AVAILABLE);
            assertThat(ticketEnProgreso.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
            
            verify(advisorRepository).incrementRecoveryCount(any());
            verify(advisorRepository).save(advisorMuerto);
            verify(ticketRepository).save(ticketEnProgreso);
            verify(rabbitTemplate).convertAndSend(eq("ticketero-exchange"), eq("caja-queue"), any(Object.class));
        }

        @Test
        @DisplayName("sin workers muertos → no debe hacer nada")
        void detectar_sinWorkersMuertos_noDebeHacerNada() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "heartbeatTimeoutSeconds", 60);
            when(advisorRepository.findDeadWorkers(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

            // When
            recoveryService.detectarYRecuperarWorkersMuertos();

            // Then
            verify(advisorRepository, never()).save(any());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("debe registrar evento de recovery")
        void detectar_debeRegistrarEvento() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "heartbeatTimeoutSeconds", 60);
            ReflectionTestUtils.setField(recoveryService, "exchangeName", "ticketero-exchange");

            Advisor advisorMuerto = advisorBusy().build();
            when(advisorRepository.findDeadWorkers(any(LocalDateTime.class)))
                .thenReturn(List.of(advisorMuerto));
            when(ticketRepository.findCurrentTicketForAdvisor(any()))
                .thenReturn(Optional.empty());

            // When
            recoveryService.detectarYRecuperarWorkersMuertos();

            // Then
            ArgumentCaptor<RecoveryEvent> captor = ArgumentCaptor.forClass(RecoveryEvent.class);
            verify(recoveryEventRepository).save(captor.capture());

            RecoveryEvent evento = captor.getValue();
            assertThat(evento.getRecoveryType()).isEqualTo("DEAD_WORKER");
            assertThat(evento.getOldAdvisorStatus()).isEqualTo("BUSY");
        }

        @Test
        @DisplayName("ticket ya completado → no debe reencolar")
        void detectar_ticketCompletado_noDebeReencolar() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "heartbeatTimeoutSeconds", 60);
            ReflectionTestUtils.setField(recoveryService, "exchangeName", "ticketero-exchange");

            Ticket ticketCompletado = ticketCompleted().build();
            Advisor advisorMuerto = advisorBusy().build();

            when(advisorRepository.findDeadWorkers(any(LocalDateTime.class)))
                .thenReturn(List.of(advisorMuerto));
            when(ticketRepository.findCurrentTicketForAdvisor(any()))
                .thenReturn(Optional.of(ticketCompletado));

            // When
            recoveryService.detectarYRecuperarWorkersMuertos();

            // Then
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        @DisplayName("debe usar routing key correcto según tipo de cola")
        void detectar_debeUsarRoutingKeyCorrecto() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "heartbeatTimeoutSeconds", 60);
            ReflectionTestUtils.setField(recoveryService, "exchangeName", "ticketero-exchange");

            Ticket ticketEnProgreso = ticketInProgress().build();
            Advisor advisorMuerto = advisorBusy().build();

            when(advisorRepository.findDeadWorkers(any(LocalDateTime.class)))
                .thenReturn(List.of(advisorMuerto));
            when(ticketRepository.findCurrentTicketForAdvisor(any()))
                .thenReturn(Optional.of(ticketEnProgreso));

            // When
            recoveryService.detectarYRecuperarWorkersMuertos();

            // Then
            verify(rabbitTemplate).convertAndSend(
                eq("ticketero-exchange"),
                eq("caja-queue"),
                any(Object.class)
            );
        }
    }

    @Nested
    @DisplayName("recuperarAsesorManual()")
    class RecuperarManual {

        @Test
        @DisplayName("debe recuperar advisor correctamente")
        void recuperarManual_debeRecuperar() {
            // Given
            ReflectionTestUtils.setField(recoveryService, "exchangeName", "ticketero-exchange");

            Advisor advisor = advisorBusy().build();
            when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));
            when(ticketRepository.findCurrentTicketForAdvisor(any()))
                .thenReturn(Optional.empty());

            // When
            recoveryService.recuperarAsesorManual(1L);

            // Then
            assertThat(advisor.getStatus()).isEqualTo(AdvisorStatus.AVAILABLE);
            
            ArgumentCaptor<RecoveryEvent> captor = ArgumentCaptor.forClass(RecoveryEvent.class);
            verify(recoveryEventRepository).save(captor.capture());
            assertThat(captor.getValue().getRecoveryType()).isEqualTo("MANUAL");
        }

        @Test
        @DisplayName("advisor inexistente → debe lanzar IllegalArgumentException")
        void recuperarManual_advisorInexistente_debeLanzarExcepcion() {
            // Given
            when(advisorRepository.findById(999L)).thenReturn(Optional.empty());

            // When + Then
            assertThatThrownBy(() -> recoveryService.recuperarAsesorManual(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
        }
    }
}
package com.example.ticketero.scheduler;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.MensajeRepository;
import com.example.ticketero.repository.TicketRepository;
import com.example.ticketero.service.TelegramService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.ticketero.testutil.TestDataBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationScheduler - Unit Tests")
class NotificationSchedulerTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Nested
    @DisplayName("enviarNotificacionesProximoTurno()")
    class EnviarNotificacionesProximoTurno {

        @Test
        @DisplayName("con tickets próximos sin notificar → debe enviar notificaciones")
        void enviarNotificaciones_conTicketsProximos_debeEnviarNotificaciones() {
            // Given
            Ticket ticket1 = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").positionInQueue(2).build();
            Ticket ticket2 = ticketWaiting().id(2L).numero("C002").telefono("+56987654321").positionInQueue(3).build();
            
            when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA))
                .thenReturn(List.of(ticket1, ticket2));
            when(mensajeRepository.existsByTicketAndPlantilla(any(), eq("totem_proximo_turno")))
                .thenReturn(false);
            when(telegramService.obtenerTextoMensaje(eq("totem_proximo_turno"), anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn("Mensaje de prueba");
            when(telegramService.enviarMensaje(anyString(), anyString()))
                .thenReturn("msg_123");

            // When
            notificationScheduler.enviarNotificacionesProximoTurno();

            // Then
            verify(telegramService, times(2)).enviarMensaje(anyString(), anyString());
            verify(mensajeRepository, times(2)).save(any(Mensaje.class));
        }

        @Test
        @DisplayName("con tickets ya notificados → no debe enviar duplicados")
        void enviarNotificaciones_conTicketsYaNotificados_noDebeEnviarDuplicados() {
            // Given
            Ticket ticket = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").positionInQueue(2).build();
            
            when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA))
                .thenReturn(List.of(ticket));
            when(mensajeRepository.existsByTicketAndPlantilla(ticket, "totem_proximo_turno"))
                .thenReturn(true);

            // When
            notificationScheduler.enviarNotificacionesProximoTurno();

            // Then
            verify(telegramService, never()).enviarMensaje(anyString(), anyString());
            verify(mensajeRepository, never()).save(any(Mensaje.class));
        }

        @Test
        @DisplayName("con tickets sin teléfono → debe omitir notificación")
        void enviarNotificaciones_conTicketsSinTelefono_debeOmitir() {
            // Given
            Ticket ticket = ticketWaiting().id(1L).numero("C001").telefono(null).positionInQueue(2).build();
            
            when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA))
                .thenReturn(List.of(ticket));

            // When
            notificationScheduler.enviarNotificacionesProximoTurno();

            // Then
            verify(telegramService, never()).enviarMensaje(anyString(), anyString());
        }

        @Test
        @DisplayName("con error en envío → debe continuar con otros tickets")
        void enviarNotificaciones_conErrorEnEnvio_debeContinuar() {
            // Given
            Ticket ticket1 = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").positionInQueue(2).build();
            Ticket ticket2 = ticketWaiting().id(2L).numero("C002").telefono("+56987654321").positionInQueue(3).build();
            
            when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA))
                .thenReturn(List.of(ticket1, ticket2));
            when(mensajeRepository.existsByTicketAndPlantilla(any(), eq("totem_proximo_turno")))
                .thenReturn(false);
            when(telegramService.obtenerTextoMensaje(anyString(), anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn("Mensaje");
            when(telegramService.enviarMensaje(eq("+56912345678"), anyString()))
                .thenThrow(new RuntimeException("Error de red"));
            when(telegramService.enviarMensaje(eq("+56987654321"), anyString()))
                .thenReturn("msg_123");

            // When
            notificationScheduler.enviarNotificacionesProximoTurno();

            // Then
            verify(telegramService, times(2)).enviarMensaje(anyString(), anyString());
            verify(mensajeRepository, times(1)).save(any(Mensaje.class)); // Solo el exitoso
        }

        @Test
        @DisplayName("con excepción general → debe manejar error sin fallar")
        void enviarNotificaciones_conExcepcionGeneral_debeManejarError() {
            // Given
            when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA))
                .thenThrow(new RuntimeException("Error de BD"));

            // When & Then - No debe lanzar excepción
            notificationScheduler.enviarNotificacionesProximoTurno();
        }
    }

    @Nested
    @DisplayName("procesarMensajesPendientes()")
    class ProcesarMensajesPendientes {

        @Test
        @DisplayName("con mensajes pendientes → debe procesarlos exitosamente")
        void procesarMensajes_conMensajesPendientes_debeProcesar() {
            // Given
            Ticket ticket = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").build();
            Mensaje mensaje = Mensaje.builder()
                .id(1L)
                .ticket(ticket)
                .plantilla("totem_ticket_creado")
                .estadoEnvio("PENDIENTE")
                .intentos(0)
                .build();
            
            when(mensajeRepository.findByEstadoEnvio("PENDIENTE"))
                .thenReturn(List.of(mensaje));
            when(telegramService.obtenerTextoMensaje(eq("totem_ticket_creado"), eq("C001"), any(), any(), any(), any()))
                .thenReturn("Mensaje de prueba");
            when(telegramService.enviarMensaje(anyString(), anyString()))
                .thenReturn("msg_123");

            // When
            notificationScheduler.procesarMensajesPendientes();

            // Then
            verify(telegramService).enviarMensaje("+56912345678", "Mensaje de prueba");
            verify(mensajeRepository).save(argThat(m -> 
                "ENVIADO".equals(m.getEstadoEnvio()) && 
                "msg_123".equals(m.getTelegramMessageId())
            ));
        }

        @Test
        @DisplayName("sin mensajes pendientes → debe terminar sin procesar")
        void procesarMensajes_sinMensajesPendientes_debeTerminar() {
            // Given
            when(mensajeRepository.findByEstadoEnvio("PENDIENTE"))
                .thenReturn(List.of());

            // When
            notificationScheduler.procesarMensajesPendientes();

            // Then
            verify(telegramService, never()).enviarMensaje(anyString(), anyString());
        }

        @Test
        @DisplayName("con error en envío → debe marcar como fallido después de 3 intentos")
        void procesarMensajes_conErrorEnEnvio_debeMarcarComoFallido() {
            // Given
            Ticket ticket = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").build();
            Mensaje mensaje = Mensaje.builder()
                .id(1L)
                .ticket(ticket)
                .plantilla("totem_ticket_creado")
                .estadoEnvio("PENDIENTE")
                .intentos(2) // Ya tiene 2 intentos
                .build();
            
            when(mensajeRepository.findByEstadoEnvio("PENDIENTE"))
                .thenReturn(List.of(mensaje));
            when(telegramService.obtenerTextoMensaje(anyString(), anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn("Mensaje");
            when(telegramService.enviarMensaje(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error de envío"));

            // When
            notificationScheduler.procesarMensajesPendientes();

            // Then
            verify(mensajeRepository).save(argThat(m -> 
                "FALLIDO".equals(m.getEstadoEnvio()) && 
                m.getIntentos() == 3
            ));
        }

        @Test
        @DisplayName("con error en envío → debe incrementar intentos si no alcanza máximo")
        void procesarMensajes_conErrorEnEnvio_debeIncrementarIntentos() {
            // Given
            Ticket ticket = ticketWaiting().id(1L).numero("C001").telefono("+56912345678").build();
            Mensaje mensaje = Mensaje.builder()
                .id(1L)
                .ticket(ticket)
                .plantilla("totem_ticket_creado")
                .estadoEnvio("PENDIENTE")
                .intentos(1) // Solo 1 intento
                .build();
            
            when(mensajeRepository.findByEstadoEnvio("PENDIENTE"))
                .thenReturn(List.of(mensaje));
            when(telegramService.obtenerTextoMensaje(anyString(), anyString(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn("Mensaje");
            when(telegramService.enviarMensaje(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error temporal"));

            // When
            notificationScheduler.procesarMensajesPendientes();

            // Then
            verify(mensajeRepository).save(argThat(m -> 
                "PENDIENTE".equals(m.getEstadoEnvio()) && 
                m.getIntentos() == 2
            ));
        }

        @Test
        @DisplayName("con excepción general → debe manejar error sin fallar")
        void procesarMensajes_conExcepcionGeneral_debeManejarError() {
            // Given
            when(mensajeRepository.findByEstadoEnvio("PENDIENTE"))
                .thenThrow(new RuntimeException("Error de BD"));

            // When & Then - No debe lanzar excepción
            notificationScheduler.procesarMensajesPendientes();
        }
    }
}
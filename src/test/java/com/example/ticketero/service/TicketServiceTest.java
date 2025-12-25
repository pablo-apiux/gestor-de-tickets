package com.example.ticketero.service;

import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.MensajeRepository;
import com.example.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.ticketero.testutil.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - Unit Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TicketService ticketService;

    // ============================================================
    // CREAR TICKET
    // ============================================================
    
    @Nested
    @DisplayName("crearTicket()")
    class CrearTicket {

        @Test
        @DisplayName("con datos válidos → debe crear ticket y notificar")
        void crearTicket_conDatosValidos_debeCrearTicketYNotificar() {
            // Given
            TicketCreateRequest request = validTicketRequest();
            Ticket ticketGuardado = ticketWaiting()
                .numero("C001")
                .positionInQueue(1)
                .estimatedWaitMinutes(5)
                .build();

            when(ticketRepository.findByNationalIdAndStatusIn(anyString(), any()))
                .thenReturn(Optional.empty());
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.EN_ESPERA))
                .thenReturn(0L);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);
            when(telegramService.obtenerTextoMensaje(anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn("Ticket creado: C001");
            when(telegramService.enviarMensaje(anyString(), anyString())).thenReturn("msg_123");

            // When
            TicketResponse response = ticketService.crearTicket(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.numero()).isEqualTo("C001");
            assertThat(response.positionInQueue()).isEqualTo(1);
            assertThat(response.estimatedWaitMinutes()).isEqualTo(5);
            assertThat(response.status()).isEqualTo(TicketStatus.EN_ESPERA);

            verify(ticketRepository).save(any(Ticket.class));
            verify(telegramService).enviarMensaje(eq("+56912345678"), anyString());
            verify(mensajeRepository).save(any(Mensaje.class));
        }

        @Test
        @DisplayName("con ticket existente activo → debe lanzar IllegalStateException")
        void crearTicket_conTicketExistente_debeLanzarExcepcion() {
            // Given
            TicketCreateRequest request = validTicketRequest();
            Ticket ticketExistente = ticketWaiting().numero("C002").build();
            
            when(ticketRepository.findByNationalIdAndStatusIn(anyString(), any()))
                .thenReturn(Optional.of(ticketExistente));

            // When + Then
            assertThatThrownBy(() -> ticketService.crearTicket(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe un ticket activo")
                .hasMessageContaining("C002");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("sin teléfono → debe crear ticket sin notificar")
        void crearTicket_sinTelefono_debeCrearSinNotificar() {
            // Given
            TicketCreateRequest request = ticketRequestSinTelefono();
            Ticket ticketGuardado = ticketWaiting().telefono(null).build();

            when(ticketRepository.findByNationalIdAndStatusIn(anyString(), any()))
                .thenReturn(Optional.empty());
            when(ticketRepository.countByQueueTypeAndStatus(any(), any())).thenReturn(0L);
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);

            // When
            TicketResponse response = ticketService.crearTicket(request);

            // Then
            assertThat(response).isNotNull();
            verify(telegramService, never()).enviarMensaje(any(), any());
            verify(mensajeRepository, never()).save(any());
        }

        @Test
        @DisplayName("con request null → debe lanzar IllegalArgumentException")
        void crearTicket_conRequestNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> ticketService.crearTicket(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request no puede ser null");
        }

        @Test
        @DisplayName("debe calcular posición correctamente")
        void crearTicket_debeCalcularPosicionCorrectamente() {
            // Given
            TicketCreateRequest request = validTicketRequest();
            Ticket ticketGuardado = ticketWaiting().build();

            when(ticketRepository.findByNationalIdAndStatusIn(anyString(), any()))
                .thenReturn(Optional.empty());
            when(ticketRepository.countByQueueTypeAndStatus(QueueType.CAJA, TicketStatus.EN_ESPERA))
                .thenReturn(3L); // 3 tickets en espera
            when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);

            // When
            ticketService.crearTicket(request);

            // Then
            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(captor.capture());

            Ticket ticket = captor.getValue();
            assertThat(ticket.getPositionInQueue()).isEqualTo(4); // 3 + 1
            assertThat(ticket.getEstimatedWaitMinutes()).isEqualTo(20); // 5 * 4
        }
    }

    // ============================================================
    // LLAMAR TICKET
    // ============================================================
    
    @Nested
    @DisplayName("llamarTicket()")
    class LlamarTicket {

        @Test
        @DisplayName("con ticket válido → debe asignar advisor y notificar")
        void llamarTicket_conTicketValido_debeAsignarYNotificar() {
            // Given
            Ticket ticket = ticketWaiting().build();
            Advisor advisor = advisorAvailable().build();

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));
            when(ticketRepository.findByQueueTypeAndStatusInOrderByCreatedAtAsc(any(), any()))
                .thenReturn(List.of());
            when(telegramService.obtenerTextoMensaje(anyString(), anyString(), any(), any(), anyString(), any()))
                .thenReturn("Es tu turno");
            when(telegramService.enviarMensaje(anyString(), anyString())).thenReturn("msg_456");

            // When
            ticketService.llamarTicket(1L, 1L);

            // Then
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
            assertThat(ticket.getAssignedAdvisor()).isEqualTo(advisor);
            assertThat(ticket.getAssignedModuleNumber()).isEqualTo(advisor.getModuleNumber());

            verify(ticketRepository).save(ticket);
            verify(telegramService).enviarMensaje(eq("+56912345678"), anyString());
        }

        @Test
        @DisplayName("con ticket no en espera → debe lanzar IllegalStateException")
        void llamarTicket_ticketNoEnEspera_debeLanzarExcepcion() {
            // Given
            Ticket ticket = ticketCompleted().build();
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // When + Then
            assertThatThrownBy(() -> ticketService.llamarTicket(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está en espera");
        }

        @Test
        @DisplayName("con IDs null → debe lanzar IllegalArgumentException")
        void llamarTicket_conIdsNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> ticketService.llamarTicket(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IDs no pueden ser null");

            assertThatThrownBy(() -> ticketService.llamarTicket(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IDs no pueden ser null");
        }
    }

    // ============================================================
    // OBTENER TICKET
    // ============================================================
    
    @Nested
    @DisplayName("obtenerTicketPorNumero()")
    class ObtenerTicket {

        @Test
        @DisplayName("con número existente → debe retornar ticket")
        void obtenerTicket_conNumeroExistente_debeRetornarTicket() {
            // Given
            Ticket ticket = ticketWaiting().numero("C001").build();
            when(ticketRepository.findByNumero("C001")).thenReturn(Optional.of(ticket));

            // When
            Optional<TicketResponse> response = ticketService.obtenerTicketPorNumero("C001");

            // Then
            assertThat(response).isPresent();
            assertThat(response.get().numero()).isEqualTo("C001");
        }

        @Test
        @DisplayName("con número inexistente → debe retornar Optional.empty()")
        void obtenerTicket_conNumeroInexistente_debeRetornarEmpty() {
            // Given
            when(ticketRepository.findByNumero("C999")).thenReturn(Optional.empty());

            // When
            Optional<TicketResponse> response = ticketService.obtenerTicketPorNumero("C999");

            // Then
            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("con número null → debe lanzar IllegalArgumentException")
        void obtenerTicket_conNumeroNull_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> ticketService.obtenerTicketPorNumero(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser null o vacío");
        }
    }
}
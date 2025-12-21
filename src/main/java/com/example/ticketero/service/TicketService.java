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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;
    private final MensajeRepository mensajeRepository;
    private final TelegramService telegramService;

    public TicketResponse crearTicket(TicketCreateRequest request) {
        QueueType queueType = request.queueType();
        
        // Obtener posición en cola
        Long posicion = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA) + 1;
        
        // Crear ticket
        Ticket ticket = Ticket.builder()
            .nationalId(request.nationalId())
            .telefono(request.telefono())
            .branchOffice(request.branchOffice())
            .queueType(queueType)
            .status(TicketStatus.EN_ESPERA)
            .positionInQueue(posicion.intValue())
            .estimatedWaitMinutes(queueType.getAvgTimeMinutes() * posicion.intValue())
            .build();
        
        ticket = ticketRepository.save(ticket);
        
        // Enviar notificación Telegram
        try {
            String texto = telegramService.obtenerTextoMensaje(
                "totem_ticket_creado",
                ticket.getNumero(),
                posicion.intValue(),
                ticket.getEstimatedWaitMinutes(),
                null,
                null
            );
            
            String messageId = telegramService.enviarMensaje(request.telefono(), texto);
            
            // Guardar mensaje
            Mensaje mensaje = new Mensaje();
            mensaje.setTicket(ticket);
            mensaje.setTelegramMessageId(messageId);
            mensaje.setPlantilla("totem_ticket_creado");
            mensajeRepository.save(mensaje);
            
        } catch (Exception e) {
            log.error("Error enviando notificación para ticket {}: {}", ticket.getNumero(), e.getMessage());
        }
        
        return new TicketResponse(
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getNationalId(),
            ticket.getTelefono(),
            ticket.getBranchOffice(),
            ticket.getQueueType(),
            ticket.getStatus(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            null,
            null,
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    public void llamarTicket(Long ticketId, Long advisorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        if (ticket.getStatus() != TicketStatus.EN_ESPERA) {
            throw new RuntimeException("El ticket no está en espera");
        }
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new RuntimeException("Asesor no encontrado"));
        
        ticket.setStatus(TicketStatus.ATENDIENDO);
        ticket.setAssignedAdvisor(advisor);
        ticket.setAssignedModuleNumber(advisor.getModuleNumber());
        
        ticketRepository.save(ticket);
        
        // Actualizar posiciones en cola
        actualizarPosicionesEnCola(ticket.getQueueType());
        
        // Enviar notificación
        try {
            String texto = telegramService.obtenerTextoMensaje(
                "totem_es_tu_turno",
                ticket.getNumero(),
                null,
                null,
                advisor.getName(),
                advisor.getModuleNumber()
            );
            
            String messageId = telegramService.enviarMensaje(ticket.getTelefono(), texto);
            
            Mensaje mensaje = new Mensaje();
            mensaje.setTicket(ticket);
            mensaje.setTelegramMessageId(messageId);
            mensaje.setPlantilla("totem_es_tu_turno");
            mensajeRepository.save(mensaje);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de llamada para ticket {}: {}", ticket.getNumero(), e.getMessage());
        }
    }

    public void finalizarTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        if (ticket.getStatus() != TicketStatus.ATENDIENDO) {
            throw new RuntimeException("El ticket no está en atención");
        }
        
        ticket.setStatus(TicketStatus.COMPLETADO);
        ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> obtenerTicketsActivos() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA)
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TicketResponse> obtenerTicketPorNumero(String numeroTicket) {
        return ticketRepository.findByNumero(numeroTicket)
            .map(this::convertirAResponse);
    }

    private void actualizarPosicionesEnCola(QueueType tipoFila) {
        List<TicketStatus> estadosActivos = List.of(TicketStatus.EN_ESPERA);
        List<Ticket> ticketsEnEspera = ticketRepository.findByQueueTypeAndStatusInOrderByCreatedAtAsc(tipoFila, estadosActivos);
        
        for (int i = 0; i < ticketsEnEspera.size(); i++) {
            Ticket ticket = ticketsEnEspera.get(i);
            ticket.setPositionInQueue(i + 1);
            ticket.setEstimatedWaitMinutes(tipoFila.getAvgTimeMinutes() * (i + 1));
        }
        
        ticketRepository.saveAll(ticketsEnEspera);
    }

    private TicketResponse convertirAResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getNationalId(),
            ticket.getTelefono(),
            ticket.getBranchOffice(),
            ticket.getQueueType(),
            ticket.getStatus(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
            ticket.getAssignedModuleNumber(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }
}
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
        // Validaciones de seguridad
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }
        
        QueueType queueType = request.queueType();
        
        // Validar que no existe ticket activo para el mismo nationalId
        List<TicketStatus> estadosActivos = TicketStatus.getActiveStatuses();
        Optional<Ticket> ticketExistente = ticketRepository.findByNationalIdAndStatusIn(
            request.nationalId(), estadosActivos);
        
        if (ticketExistente.isPresent()) {
            throw new IllegalStateException("Ya existe un ticket activo para este RUT/ID: " + 
                ticketExistente.get().getNumero());
        }
        
        // Obtener posición en cola
        Long posicion = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA) + 1;
        
        // Crear ticket
        Ticket ticket = Ticket.builder()
            .nationalId(request.nationalId().trim())
            .telefono(request.telefono() != null ? request.telefono().trim() : null)
            .branchOffice(request.branchOffice().trim())
            .queueType(queueType)
            .status(TicketStatus.EN_ESPERA)
            .positionInQueue(posicion.intValue())
            .estimatedWaitMinutes(queueType.getAvgTimeMinutes() * posicion.intValue())
            .build();
        
        ticket = ticketRepository.save(ticket);
        
        // Enviar notificación Telegram solo si hay teléfono
        if (ticket.getTelefono() != null && !ticket.getTelefono().isEmpty()) {
            enviarNotificacionCreacion(ticket, posicion.intValue());
        }
        
        return convertirAResponse(ticket);
    }

    public void llamarTicket(Long ticketId, Long advisorId) {
        if (ticketId == null || advisorId == null) {
            throw new IllegalArgumentException("IDs no pueden ser null");
        }
        
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + ticketId));
        
        if (ticket.getStatus() != TicketStatus.EN_ESPERA) {
            throw new IllegalStateException("El ticket no está en espera. Estado actual: " + ticket.getStatus());
        }
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new IllegalArgumentException("Asesor no encontrado con ID: " + advisorId));
        
        ticket.setStatus(TicketStatus.ATENDIENDO);
        ticket.setAssignedAdvisor(advisor);
        ticket.setAssignedModuleNumber(advisor.getModuleNumber());
        
        ticketRepository.save(ticket);
        
        // Actualizar posiciones en cola
        actualizarPosicionesEnCola(ticket.getQueueType());
        
        // Enviar notificación
        if (ticket.getTelefono() != null && !ticket.getTelefono().isEmpty()) {
            enviarNotificacionLlamada(ticket, advisor);
        }
    }

    public void finalizarTicket(Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("ID del ticket no puede ser null");
        }
        
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + ticketId));
        
        if (ticket.getStatus() != TicketStatus.ATENDIENDO) {
            throw new IllegalStateException("The ticket is not being attended. Current status: " + ticket.getStatus());
        }
        
        ticket.setStatus(TicketStatus.COMPLETADO);
        ticketRepository.save(ticket);
        
        log.info("Ticket {} finalizado exitosamente", ticket.getNumero());
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
        if (numeroTicket == null || numeroTicket.trim().isEmpty()) {
            throw new IllegalArgumentException("Número de ticket no puede ser null o vacío");
        }
        
        return ticketRepository.findByNumero(numeroTicket.trim().toUpperCase())
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
    
    private void enviarNotificacionCreacion(Ticket ticket, Integer posicion) {
        try {
            String texto = telegramService.obtenerTextoMensaje(
                "totem_ticket_creado",
                ticket.getNumero(),
                posicion,
                ticket.getEstimatedWaitMinutes(),
                null,
                null
            );
            
            String messageId = telegramService.enviarMensaje(ticket.getTelefono(), texto);
            
            Mensaje mensaje = Mensaje.builder()
                .ticket(ticket)
                .telegramMessageId(messageId)
                .plantilla("totem_ticket_creado")
                .build();
            mensajeRepository.save(mensaje);
            
        } catch (Exception e) {
            log.error("Error enviando notificación para ticket {}: {}", ticket.getNumero(), e.getMessage());
        }
    }
    
    private void enviarNotificacionLlamada(Ticket ticket, Advisor advisor) {
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
            
            Mensaje mensaje = Mensaje.builder()
                .ticket(ticket)
                .telegramMessageId(messageId)
                .plantilla("totem_es_tu_turno")
                .build();
            mensajeRepository.save(mensaje);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de llamada para ticket {}: {}", ticket.getNumero(), e.getMessage());
        }
    }
}
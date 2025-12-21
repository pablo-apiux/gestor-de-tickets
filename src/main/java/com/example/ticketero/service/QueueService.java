package com.example.ticketero.service;

import com.example.ticketero.model.dto.QueueStatusResponse;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio para gestión de colas de atención
 * Proporciona información sobre el estado de las colas y tickets en espera
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QueueService {

    private final TicketRepository ticketRepository;
    private static final int MAX_TICKETS_DISPLAY = 10;

    /**
     * Obtiene el estado de todas las colas del sistema
     * @return Lista con el estado de cada tipo de cola
     */
    public List<QueueStatusResponse> obtenerEstadoColas() {
        log.debug("Obteniendo estado de todas las colas");
        return Arrays.stream(QueueType.values())
            .map(this::crearEstadoCola)
            .toList();
    }

    /**
     * Obtiene el estado de una cola específica
     * @param queueType Tipo de cola a consultar
     * @return Estado de la cola especificada
     */
    public QueueStatusResponse obtenerEstadoCola(QueueType queueType) {
        if (queueType == null) {
            throw new IllegalArgumentException("Tipo de cola no puede ser null");
        }
        
        log.debug("Obteniendo estado de la cola: {}", queueType.getDisplayName());
        return crearEstadoCola(queueType);
    }

    /**
     * Obtiene los próximos tickets en una cola
     * @param queueType Tipo de cola
     * @param limite Número máximo de tickets a retornar
     * @return Lista de tickets en espera
     */
    public List<Ticket> obtenerProximosTickets(QueueType queueType, int limite) {
        if (queueType == null) {
            throw new IllegalArgumentException("Tipo de cola no puede ser null");
        }
        if (limite <= 0) {
            throw new IllegalArgumentException("Límite debe ser mayor a 0");
        }
        
        List<TicketStatus> estadosActivos = List.of(TicketStatus.EN_ESPERA);
        List<Ticket> tickets = ticketRepository.findNextTicketsForQueue(queueType, estadosActivos);
        
        int ticketsToReturn = Math.min(limite, tickets.size());
        log.debug("Obteniendo {} tickets de {} disponibles para cola {}", 
            ticketsToReturn, tickets.size(), queueType.getDisplayName());
            
        return tickets.stream().limit(limite).toList();
    }

    /**
     * Crea el estado de una cola específica
     * @param queueType Tipo de cola
     * @return Estado completo de la cola
     */
    private QueueStatusResponse crearEstadoCola(QueueType queueType) {
        Long ticketsEnEspera = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA);
        Long ticketsAtendiendo = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.ATENDIENDO);
        Long totalTickets = ticketsEnEspera + ticketsAtendiendo;
        
        int tiempoEstimado = Math.toIntExact(ticketsEnEspera * queueType.getAvgTimeMinutes());
        
        List<Ticket> ticketsEspera = obtenerProximosTickets(queueType, MAX_TICKETS_DISPLAY);
        List<QueueStatusResponse.TicketInfo> waitingList = ticketsEspera.stream()
            .map(ticket -> new QueueStatusResponse.TicketInfo(
                ticket.getNumero(),
                ticket.getPositionInQueue(),
                ticket.getEstimatedWaitMinutes(),
                ticket.getCreatedAt()
            ))
            .toList();
        
        log.debug("Estado cola {}: {} en espera, {} atendiendo, {} min estimado", 
            queueType.getDisplayName(), ticketsEnEspera, ticketsAtendiendo, tiempoEstimado);
        
        return new QueueStatusResponse(
            queueType,
            queueType.getDisplayName(),
            queueType.getAvgTimeMinutes(),
            queueType.getPriority(),
            String.valueOf(queueType.getPrefix()),
            Math.toIntExact(totalTickets),
            Math.toIntExact(ticketsEnEspera),
            Math.toIntExact(ticketsAtendiendo),
            tiempoEstimado,
            java.time.LocalDateTime.now(),
            waitingList
        );
    }
}
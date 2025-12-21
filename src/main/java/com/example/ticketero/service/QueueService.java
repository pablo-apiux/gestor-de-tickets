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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QueueService {

    private final TicketRepository ticketRepository;

    public List<QueueStatusResponse> obtenerEstadoColas() {
        return Arrays.stream(QueueType.values())
            .map(this::crearEstadoCola)
            .toList();
    }

    public QueueStatusResponse obtenerEstadoCola(QueueType queueType) {
        return crearEstadoCola(queueType);
    }

    public List<Ticket> obtenerProximosTickets(QueueType queueType, int limite) {
        List<TicketStatus> estadosActivos = List.of(TicketStatus.EN_ESPERA);
        List<Ticket> tickets = ticketRepository.findNextTicketsForQueue(queueType, estadosActivos);
        return tickets.stream().limit(limite).toList();
    }

    private QueueStatusResponse crearEstadoCola(QueueType queueType) {
        Long ticketsEnEspera = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA);
        Long ticketsAtendiendo = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.ATENDIENDO);
        Long totalTickets = ticketsEnEspera + ticketsAtendiendo;
        
        int tiempoEstimado = ticketsEnEspera.intValue() * queueType.getAvgTimeMinutes();
        
        List<Ticket> ticketsEspera = obtenerProximosTickets(queueType, 10);
        List<QueueStatusResponse.TicketInfo> waitingList = ticketsEspera.stream()
            .map(ticket -> new QueueStatusResponse.TicketInfo(
                ticket.getNumero(),
                ticket.getPositionInQueue(),
                ticket.getEstimatedWaitMinutes(),
                ticket.getCreatedAt()
            ))
            .toList();
        
        return new QueueStatusResponse(
            queueType,
            queueType.getDisplayName(),
            queueType.getAvgTimeMinutes(),
            queueType.getPriority(),
            String.valueOf(queueType.getPrefix()),
            totalTickets.intValue(),
            ticketsEnEspera.intValue(),
            ticketsAtendiendo.intValue(),
            tiempoEstimado,
            java.time.LocalDateTime.now(),
            waitingList
        );
    }
}
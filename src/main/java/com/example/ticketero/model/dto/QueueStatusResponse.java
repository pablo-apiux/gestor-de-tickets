package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;

import java.time.LocalDateTime;
import java.util.List;

public record QueueStatusResponse(
    QueueType queueType,
    String displayName,
    Integer averageWaitMinutes,
    Integer priority,
    String prefix,
    Integer totalTickets,
    Integer waitingTickets,
    Integer attendingTickets,
    Integer estimatedWaitTime,
    LocalDateTime lastUpdated,
    List<TicketInfo> waitingList
) {
    
    public record TicketInfo(
        String numero,
        Integer positionInQueue,
        Integer estimatedWaitMinutes,
        LocalDateTime createdAt
    ) {}
}
package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;

import java.time.LocalDateTime;

public record QueuePositionResponse(
    String numero,
    TicketStatus status,
    QueueType queueType,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    String assignedAdvisor,
    Integer assignedModuleNumber,
    String message,
    LocalDateTime lastUpdated
) {}
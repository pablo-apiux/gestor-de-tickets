package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;

public record TicketQueueMessage(
    Long ticketId,
    String numero,
    QueueType queueType,
    String telefono,
    String branchOffice,
    Integer positionInQueue,
    Integer estimatedWaitMinutes
) {}
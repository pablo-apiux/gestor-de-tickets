package com.example.ticketero.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
    LocalDateTime timestamp,
    SummaryData summary,
    AdvisorData advisors,
    List<QueueData> queues,
    List<AlertData> alerts
) {
    
    public record SummaryData(
        Integer totalTicketsToday,
        Integer waitingTickets,
        Integer attendingTickets,
        Integer completedTickets,
        Integer averageWaitTimeMinutes,
        String peakHour
    ) {}
    
    public record AdvisorData(
        Integer available,
        Integer busy,
        Integer offline,
        Integer totalCapacity
    ) {}
    
    public record QueueData(
        String queueType,
        String displayName,
        Integer waitingTickets,
        Integer attendingTickets,
        Integer completedToday,
        Integer maxWaitTimeMinutes,
        Integer averageServiceMinutes,
        String status
    ) {}
    
    public record AlertData(
        String type,
        String message,
        String severity,
        LocalDateTime timestamp,
        String suggestedAction
    ) {}
}
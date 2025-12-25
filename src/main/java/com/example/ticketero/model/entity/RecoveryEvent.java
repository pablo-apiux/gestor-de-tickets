package com.example.ticketero.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recovery_type", nullable = false, length = 50)
    private String recoveryType;

    @Column(name = "advisor_id")
    private Long advisorId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "old_advisor_status", length = 20)
    private String oldAdvisorStatus;

    @Column(name = "new_advisor_status", length = 20)
    private String newAdvisorStatus;

    @Column(name = "old_ticket_status", length = 20)
    private String oldTicketStatus;

    @Column(name = "new_ticket_status", length = 20)
    private String newTicketStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
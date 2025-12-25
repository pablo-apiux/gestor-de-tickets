package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoReferencia(UUID codigoReferencia);

    Optional<Ticket> findByNumero(String numero);

    @Query("SELECT t FROM Ticket t WHERE t.nationalId = :nationalId AND t.status IN :statuses")
    Optional<Ticket> findByNationalIdAndStatusIn(
        @Param("nationalId") String nationalId, 
        @Param("statuses") List<TicketStatus> statuses
    );

    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.queueType = :queueType AND t.status IN :statuses ORDER BY t.createdAt ASC")
    List<Ticket> findByQueueTypeAndStatusInOrderByCreatedAtAsc(
        @Param("queueType") QueueType queueType,
        @Param("statuses") List<TicketStatus> statuses
    );

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.queueType = :queueType AND t.status IN :statuses AND t.createdAt < :createdAt")
    Long countByQueueTypeAndStatusInAndCreatedAtBefore(
        @Param("queueType") QueueType queueType,
        @Param("statuses") List<TicketStatus> statuses,
        @Param("createdAt") LocalDateTime createdAt
    );

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    Long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.queueType = :queueType AND t.status = :status")
    Long countByQueueTypeAndStatus(
        @Param("queueType") QueueType queueType,
        @Param("status") TicketStatus status
    );

    @Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.queueType = :queueType ORDER BY t.createdAt ASC")
    List<Ticket> findNextTicketsForQueue(
        @Param("queueType") QueueType queueType,
        @Param("statuses") List<TicketStatus> statuses
    );
    
    // Métodos adicionales para dashboard
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :fecha")
    Long countByCreatedAtAfter(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND t.createdAt >= :fecha")
    Long countByStatusAndCreatedAtAfter(
        @Param("status") TicketStatus status,
        @Param("fecha") LocalDateTime fecha
    );
    
    @Query(value = "SELECT COUNT(t) FROM Ticket t WHERE t.queueType = :queueType AND t.status = :status AND t.createdAt >= :fecha", 
           nativeQuery = false)
    Long countByQueueTypeAndStatusAndCreatedAtAfter(
        @Param("queueType") QueueType queueType,
        @Param("status") TicketStatus status,
        @Param("fecha") LocalDateTime fecha
    );

    // Métodos para Recovery Service
    @Query("SELECT t FROM Ticket t WHERE t.assignedAdvisor.id = :advisorId AND t.status IN ('ATENDIENDO', 'EN_ESPERA')")
    Optional<Ticket> findCurrentTicketForAdvisor(@Param("advisorId") Long advisorId);
}
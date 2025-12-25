package com.example.ticketero.repository;

import com.example.ticketero.model.entity.OutboxMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'PENDING' AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingWithLock(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = 'SENT', o.processedAt = :processedAt WHERE o.id = :id")
    void markAsSent(@Param("id") Long id, @Param("processedAt") LocalDateTime processedAt);

    @Modifying
    @Query("UPDATE OutboxMessage o SET o.retryCount = :retryCount, o.nextRetryAt = :nextRetryAt, o.errorMessage = :errorMessage WHERE o.id = :id")
    void scheduleRetry(@Param("id") Long id, @Param("retryCount") Integer retryCount, 
                      @Param("nextRetryAt") LocalDateTime nextRetryAt, @Param("errorMessage") String errorMessage);

    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = 'FAILED', o.errorMessage = :errorMessage, o.processedAt = :processedAt WHERE o.id = :id")
    void markAsFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage, @Param("processedAt") LocalDateTime processedAt);
}
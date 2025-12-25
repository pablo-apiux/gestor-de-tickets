package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

    @Query("SELECT a FROM Advisor a WHERE a.status = :status ORDER BY a.assignedTicketsCount ASC, a.updatedAt ASC")
    List<Advisor> findByStatusOrderByAssignedTicketsCountAscUpdatedAtAsc(@Param("status") AdvisorStatus status);

    @Query("SELECT a FROM Advisor a WHERE a.status = :status ORDER BY a.assignedTicketsCount ASC")
    Optional<Advisor> findFirstByStatusOrderByAssignedTicketsCountAsc(@Param("status") AdvisorStatus status);

    @Query("SELECT COUNT(a) FROM Advisor a WHERE a.status = :status")
    Long countByStatus(@Param("status") AdvisorStatus status);

    @Query("SELECT a FROM Advisor a WHERE a.moduleNumber = :moduleNumber")
    Optional<Advisor> findByModuleNumber(@Param("moduleNumber") Integer moduleNumber);

    Optional<Advisor> findByEmail(String email);

    @Query("SELECT a FROM Advisor a ORDER BY a.moduleNumber ASC")
    List<Advisor> findAllOrderByModuleNumberAsc();

    @Query("SELECT AVG(a.assignedTicketsCount) FROM Advisor a WHERE a.status = :status")
    Double getAverageAssignedTicketsCountByStatus(@Param("status") AdvisorStatus status);

    // Métodos para Recovery Service
    @Query("SELECT a FROM Advisor a WHERE a.status = 'BUSY' AND a.updatedAt < :timeoutThreshold")
    List<Advisor> findDeadWorkers(@Param("timeoutThreshold") java.time.LocalDateTime timeoutThreshold);

    @Modifying
    @Query("UPDATE Advisor a SET a.assignedTicketsCount = a.assignedTicketsCount + 1 WHERE a.id = :advisorId")
    void incrementRecoveryCount(@Param("advisorId") Long advisorId);
}
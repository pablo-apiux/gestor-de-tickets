package com.example.ticketero.repository;

import com.example.ticketero.model.entity.RecoveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecoveryEventRepository extends JpaRepository<RecoveryEvent, Long> {
}
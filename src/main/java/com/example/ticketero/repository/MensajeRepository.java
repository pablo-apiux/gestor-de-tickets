package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE m.estadoEnvio = :estado AND m.fechaProgramada <= :fecha ORDER BY m.fechaProgramada ASC")
    List<Mensaje> findByEstadoEnvioAndFechaProgramadaLessThanEqual(
        @Param("estado") String estado,
        @Param("fecha") LocalDateTime fecha
    );

    @Query("SELECT m FROM Mensaje m WHERE m.ticket.id = :ticketId ORDER BY m.fechaProgramada ASC")
    List<Mensaje> findByTicketIdOrderByFechaProgramadaAsc(@Param("ticketId") Long ticketId);

    @Query("SELECT m FROM Mensaje m WHERE m.estadoEnvio = :estado AND m.intentos < :maxIntentos")
    List<Mensaje> findByEstadoEnvioAndIntentosLessThan(
        @Param("estado") String estado,
        @Param("maxIntentos") Integer maxIntentos
    );

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.estadoEnvio = :estado")
    Long countByEstadoEnvio(@Param("estado") String estado);

    @Query("SELECT m FROM Mensaje m WHERE m.ticket.numero = :numeroTicket ORDER BY m.fechaProgramada ASC")
    List<Mensaje> findByTicketNumeroOrderByFechaProgramadaAsc(@Param("numeroTicket") String numeroTicket);
}
package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.entity.RecoveryEvent;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.RecoveryEventRepository;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryService {

    private final AdvisorRepository advisorRepository;
    private final TicketRepository ticketRepository;
    private final RecoveryEventRepository recoveryEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${recovery.heartbeat-timeout-seconds:300}")
    private int heartbeatTimeoutSeconds;

    @Value("${rabbitmq.exchange.name:ticketero-exchange}")
    private String exchangeName;

    @Scheduled(fixedDelayString = "${recovery.check-interval:60000}")
    @Transactional
    public void detectarYRecuperarWorkersMuertos() {
        log.debug("Iniciando detección de workers muertos");
        
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        List<Advisor> deadWorkers = advisorRepository.findDeadWorkers(timeoutThreshold);

        if (deadWorkers.isEmpty()) {
            log.debug("No se encontraron workers muertos");
            return;
        }

        log.warn("Detectados {} workers muertos", deadWorkers.size());

        for (Advisor deadWorker : deadWorkers) {
            try {
                recuperarWorker(deadWorker, "DEAD_WORKER");
            } catch (Exception e) {
                log.error("Error recuperando worker {}: {}", deadWorker.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void recuperarAsesorManual(Long advisorId) {
        log.info("Iniciando recuperación manual del advisor {}", advisorId);
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new IllegalArgumentException("Advisor no encontrado: " + advisorId));
        
        recuperarWorker(advisor, "MANUAL");
    }

    private void recuperarWorker(Advisor advisor, String recoveryType) {
        log.info("Recuperando worker: {} ({})", advisor.getId(), recoveryType);
        
        String oldStatus = advisor.getStatus().name();
        
        // Buscar ticket actual del advisor
        Optional<Ticket> currentTicket = ticketRepository.findCurrentTicketForAdvisor(advisor.getId());
        
        String oldTicketStatus = null;
        String newTicketStatus = null;
        
        if (currentTicket.isPresent()) {
            Ticket ticket = currentTicket.get();
            oldTicketStatus = ticket.getStatus().name();
            
            // Solo reencolar si no está completado
            if (ticket.getStatus() != TicketStatus.COMPLETADO) {
                ticket.setStatus(TicketStatus.EN_ESPERA);
                ticket.setAssignedAdvisor(null);
                ticket.setAssignedModuleNumber(null);
                ticketRepository.save(ticket);
                
                newTicketStatus = TicketStatus.EN_ESPERA.name();
                
                // Reencolar el ticket
                try {
                    rabbitTemplate.convertAndSend(
                        exchangeName,
                        getRoutingKeyForQueue(ticket.getQueueType().name()),
                        createRequeueMessage(ticket)
                    );
                    log.info("Ticket {} reencolado exitosamente", ticket.getNumero());
                } catch (Exception e) {
                    log.error("Error reencolando ticket {}: {}", ticket.getNumero(), e.getMessage());
                }
            }
        }
        
        // Liberar advisor
        advisor.setStatus(AdvisorStatus.AVAILABLE);
        advisorRepository.incrementRecoveryCount(advisor.getId());
        advisorRepository.save(advisor);
        
        // Registrar evento de recuperación
        RecoveryEvent event = RecoveryEvent.builder()
            .recoveryType(recoveryType)
            .advisorId(advisor.getId())
            .ticketId(currentTicket.map(Ticket::getId).orElse(null))
            .oldAdvisorStatus(oldStatus)
            .newAdvisorStatus(AdvisorStatus.AVAILABLE.name())
            .oldTicketStatus(oldTicketStatus)
            .newTicketStatus(newTicketStatus)
            .reason("Worker recovery - timeout or manual intervention")
            .build();
        
        recoveryEventRepository.save(event);
        
        log.info("Worker {} recuperado exitosamente", advisor.getId());
    }

    private String getRoutingKeyForQueue(String queueType) {
        return switch (queueType) {
            case "CAJA" -> "caja-queue";
            case "PERSONAL_BANKER" -> "personal-queue";
            case "EMPRESAS" -> "empresas-queue";
            case "GERENCIA" -> "gerencia-queue";
            default -> "default-queue";
        };
    }

    private Object createRequeueMessage(Ticket ticket) {
        return java.util.Map.of(
            "ticketId", ticket.getId(),
            "numero", ticket.getNumero(),
            "queueType", ticket.getQueueType().name(),
            "action", "REQUEUE",
            "reason", "WORKER_RECOVERY"
        );
    }
}
package com.example.ticketero.service;

import com.example.ticketero.model.dto.TicketQueueMessage;
import com.example.ticketero.model.entity.OutboxMessage;
import com.example.ticketero.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxMessageRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.name:ticketero-exchange}")
    private String exchangeName;

    @Value("${outbox.batch-size:10}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.process-interval:5000}")
    @Transactional
    public void processOutbox() {
        log.debug("Procesando mensajes pendientes del Outbox");
        
        List<OutboxMessage> pendingMessages = outboxRepository.findPendingWithLock(
            LocalDateTime.now(), 
            PageRequest.of(0, batchSize)
        );

        if (pendingMessages.isEmpty()) {
            log.debug("No hay mensajes pendientes en el Outbox");
            return;
        }

        log.info("Procesando {} mensajes del Outbox", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                publishMessage(message);
                outboxRepository.markAsSent(message.getId(), LocalDateTime.now());
                log.debug("Mensaje {} publicado exitosamente", message.getId());
                
            } catch (Exception e) {
                handlePublishError(message, e);
            }
        }
    }

    private void publishMessage(OutboxMessage message) throws Exception {
        TicketQueueMessage queueMessage = objectMapper.readValue(message.getPayload(), TicketQueueMessage.class);
        
        rabbitTemplate.convertAndSend(
            exchangeName,
            message.getRoutingKey(),
            queueMessage
        );
        
        log.debug("Mensaje publicado: exchange={}, routingKey={}, messageId={}", 
            exchangeName, message.getRoutingKey(), message.getId());
    }

    private void handlePublishError(OutboxMessage message, Exception e) {
        log.error("Error publicando mensaje {}: {}", message.getId(), e.getMessage());
        
        int newRetryCount = message.getRetryCount() + 1;
        
        if (newRetryCount >= message.getMaxRetries()) {
            outboxRepository.markAsFailed(
                message.getId(), 
                "Max retries exceeded: " + e.getMessage(),
                LocalDateTime.now()
            );
            log.warn("Mensaje {} marcado como FAILED después de {} intentos", message.getId(), newRetryCount);
        } else {
            LocalDateTime nextRetry = calculateNextRetry(newRetryCount);
            outboxRepository.scheduleRetry(
                message.getId(),
                newRetryCount,
                nextRetry,
                e.getMessage()
            );
            log.info("Mensaje {} programado para reintento #{} en {}", message.getId(), newRetryCount, nextRetry);
        }
    }

    private LocalDateTime calculateNextRetry(int retryCount) {
        // Exponential backoff: 2^retryCount seconds
        long delaySeconds = (long) Math.pow(2, retryCount);
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
package com.example.ticketero.scheduler;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.MensajeRepository;
import com.example.ticketero.repository.TicketRepository;
import com.example.ticketero.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final TicketRepository ticketRepository;
    private final MensajeRepository mensajeRepository;
    private final TelegramService telegramService;

    @Scheduled(fixedRate = 30000) // Cada 30 segundos
    public void enviarNotificacionesProximoTurno() {
        List<Ticket> ticketsProximos = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA)
            .stream()
            .filter(ticket -> ticket.getPositionInQueue() <= 3)
            .toList();

        for (Ticket ticket : ticketsProximos) {
            if (!yaSeEnvioNotificacionProximo(ticket)) {
                enviarNotificacionProximo(ticket);
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Cada minuto
    public void procesarMensajesPendientes() {
        List<Mensaje> mensajesPendientes = mensajeRepository.findByEstadoEnvio("PENDIENTE");
        
        for (Mensaje mensaje : mensajesPendientes) {
            try {
                String messageId = telegramService.enviarMensaje(
                    mensaje.getTicket().getTelefono(),
                    obtenerTextoMensaje(mensaje)
                );
                
                mensaje.setTelegramMessageId(messageId);
                mensaje.setEstadoEnvio("ENVIADO");
                mensaje.setFechaEnvio(LocalDateTime.now());
                mensajeRepository.save(mensaje);
                
            } catch (Exception e) {
                mensaje.setIntentos(mensaje.getIntentos() + 1);
                if (mensaje.getIntentos() >= 3) {
                    mensaje.setEstadoEnvio("FALLIDO");
                }
                mensajeRepository.save(mensaje);
                log.error("Error enviando mensaje {}: {}", mensaje.getId(), e.getMessage());
            }
        }
    }

    private boolean yaSeEnvioNotificacionProximo(Ticket ticket) {
        return mensajeRepository.existsByTicketAndPlantilla(ticket, "totem_proximo_turno");
    }

    private void enviarNotificacionProximo(Ticket ticket) {
        try {
            String texto = telegramService.obtenerTextoMensaje(
                "totem_proximo_turno",
                ticket.getNumero(),
                null,
                null,
                null,
                null
            );
            
            String messageId = telegramService.enviarMensaje(ticket.getTelefono(), texto);
            
            Mensaje mensaje = new Mensaje();
            mensaje.setTicket(ticket);
            mensaje.setPlantilla("totem_proximo_turno");
            mensaje.setTelegramMessageId(messageId);
            mensaje.setEstadoEnvio("ENVIADO");
            mensaje.setFechaEnvio(LocalDateTime.now());
            mensaje.setFechaProgramada(LocalDateTime.now());
            mensajeRepository.save(mensaje);
            
        } catch (Exception e) {
            log.error("Error enviando notificación próximo turno para ticket {}: {}", ticket.getNumero(), e.getMessage());
        }
    }

    private String obtenerTextoMensaje(Mensaje mensaje) {
        Ticket ticket = mensaje.getTicket();
        return telegramService.obtenerTextoMensaje(
            mensaje.getPlantilla(),
            ticket.getNumero(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
            ticket.getAssignedModuleNumber()
        );
    }
}
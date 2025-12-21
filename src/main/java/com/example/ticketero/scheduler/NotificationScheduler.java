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

/**
 * Scheduler para el envío automático de notificaciones
 * Procesa mensajes pendientes y envía alertas de próximo turno
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final TicketRepository ticketRepository;
    private final MensajeRepository mensajeRepository;
    private final TelegramService telegramService;
    
    private static final int POSICION_PROXIMO_TURNO = 3;
    private static final int MAX_INTENTOS_ENVIO = 3;

    /**
     * Envía notificaciones a tickets que están próximos a ser atendidos
     * Ejecuta cada 30 segundos
     */
    @Scheduled(fixedRate = 30000)
    public void enviarNotificacionesProximoTurno() {
        log.debug("Iniciando proceso de notificaciones próximo turno");
        
        try {
            List<Ticket> ticketsProximos = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA)
                .stream()
                .filter(ticket -> ticket.getPositionInQueue() <= POSICION_PROXIMO_TURNO)
                .filter(ticket -> ticket.getTelefono() != null && !ticket.getTelefono().isEmpty())
                .toList();

            log.debug("Encontrados {} tickets próximos para notificar", ticketsProximos.size());
            
            int notificacionesEnviadas = 0;
            for (Ticket ticket : ticketsProximos) {
                if (!yaSeEnvioNotificacionProximo(ticket)) {
                    enviarNotificacionProximo(ticket);
                    notificacionesEnviadas++;
                }
            }
            
            if (notificacionesEnviadas > 0) {
                log.info("Enviadas {} notificaciones de próximo turno", notificacionesEnviadas);
            }
            
        } catch (Exception e) {
            log.error("Error en proceso de notificaciones próximo turno: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa mensajes pendientes de envío
     * Ejecuta cada minuto
     */
    @Scheduled(fixedRate = 60000)
    public void procesarMensajesPendientes() {
        log.debug("Iniciando procesamiento de mensajes pendientes");
        
        try {
            List<Mensaje> mensajesPendientes = mensajeRepository.findByEstadoEnvio("PENDIENTE");
            
            if (mensajesPendientes.isEmpty()) {
                log.debug("No hay mensajes pendientes para procesar");
                return;
            }
            
            log.debug("Procesando {} mensajes pendientes", mensajesPendientes.size());
            
            int mensajesEnviados = 0;
            int mensajesFallidos = 0;
            
            for (Mensaje mensaje : mensajesPendientes) {
                try {
                    procesarMensajePendiente(mensaje);
                    mensajesEnviados++;
                    
                } catch (Exception e) {
                    manejarErrorEnvioMensaje(mensaje, e);
                    mensajesFallidos++;
                }
            }
            
            log.info("Mensajes procesados: {} enviados, {} fallidos", mensajesEnviados, mensajesFallidos);
            
        } catch (Exception e) {
            log.error("Error en procesamiento de mensajes pendientes: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica si ya se envió notificación de próximo turno para un ticket
     */
    private boolean yaSeEnvioNotificacionProximo(Ticket ticket) {
        return mensajeRepository.existsByTicketAndPlantilla(ticket, "totem_proximo_turno");
    }

    /**
     * Envía notificación de próximo turno
     */
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
            
            Mensaje mensaje = Mensaje.builder()
                .ticket(ticket)
                .plantilla("totem_proximo_turno")
                .telegramMessageId(messageId)
                .estadoEnvio("ENVIADO")
                .fechaEnvio(LocalDateTime.now())
                .fechaProgramada(LocalDateTime.now())
                .build();
            mensajeRepository.save(mensaje);
            
            log.debug("Notificación próximo turno enviada para ticket {}", ticket.getNumero());
            
        } catch (Exception e) {
            log.error("Error enviando notificación próximo turno para ticket {}: {}", 
                ticket.getNumero(), e.getMessage());
        }
    }

    /**
     * Procesa un mensaje pendiente individual
     */
    private void procesarMensajePendiente(Mensaje mensaje) {
        String messageId = telegramService.enviarMensaje(
            mensaje.getTicket().getTelefono(),
            obtenerTextoMensaje(mensaje)
        );
        
        mensaje.setTelegramMessageId(messageId);
        mensaje.setEstadoEnvio("ENVIADO");
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensajeRepository.save(mensaje);
        
        log.debug("Mensaje {} enviado exitosamente", mensaje.getId());
    }

    /**
     * Maneja errores en el envío de mensajes
     */
    private void manejarErrorEnvioMensaje(Mensaje mensaje, Exception e) {
        mensaje.setIntentos(mensaje.getIntentos() + 1);
        
        if (mensaje.getIntentos() >= MAX_INTENTOS_ENVIO) {
            mensaje.setEstadoEnvio("FALLIDO");
            log.warn("Mensaje {} marcado como fallido después de {} intentos", 
                mensaje.getId(), MAX_INTENTOS_ENVIO);
        } else {
            log.debug("Intento {} fallido para mensaje {}: {}", 
                mensaje.getIntentos(), mensaje.getId(), e.getMessage());
        }
        
        mensajeRepository.save(mensaje);
    }

    /**
     * Obtiene el texto del mensaje basado en la plantilla
     */
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
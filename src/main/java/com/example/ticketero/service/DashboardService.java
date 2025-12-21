package com.example.ticketero.service;

import com.example.ticketero.model.dto.DashboardResponse;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.AdvisorRepository;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para generar métricas del dashboard administrativo
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;

    /**
     * Genera el dashboard completo con métricas del sistema
     */
    public DashboardResponse obtenerDashboard() {
        log.debug("Generando métricas del dashboard");
        
        LocalDateTime inicioDelDia = LocalDateTime.now().with(LocalTime.MIN);
        
        return new DashboardResponse(
            LocalDateTime.now(),
            generarSummaryData(inicioDelDia),
            generarAdvisorData(),
            generarQueueData(),
            generarAlerts()
        );
    }

    private DashboardResponse.SummaryData generarSummaryData(LocalDateTime inicioDelDia) {
        Long totalHoy = ticketRepository.countByCreatedAtAfter(inicioDelDia);
        Long enEspera = ticketRepository.countByStatus(TicketStatus.EN_ESPERA);
        Long atendiendo = ticketRepository.countByStatus(TicketStatus.ATENDIENDO);
        Long completados = ticketRepository.countByStatusAndCreatedAtAfter(TicketStatus.COMPLETADO, inicioDelDia);
        
        return new DashboardResponse.SummaryData(
            totalHoy.intValue(),
            enEspera.intValue(),
            atendiendo.intValue(),
            completados.intValue(),
            15, // Tiempo promedio estimado
            "10:00-11:00" // Hora pico estimada
        );
    }

    private DashboardResponse.AdvisorData generarAdvisorData() {
        Long disponibles = advisorRepository.countByStatus(AdvisorStatus.AVAILABLE);
        Long ocupados = advisorRepository.countByStatus(AdvisorStatus.BUSY);
        Long offline = advisorRepository.countByStatus(AdvisorStatus.OFFLINE);
        Long total = advisorRepository.count();
        
        return new DashboardResponse.AdvisorData(
            disponibles.intValue(),
            ocupados.intValue(),
            offline.intValue(),
            total.intValue()
        );
    }

    private List<DashboardResponse.QueueData> generarQueueData() {
        return Arrays.stream(QueueType.values())
            .map(this::generarQueueDataPorTipo)
            .toList();
    }

    private DashboardResponse.QueueData generarQueueDataPorTipo(QueueType queueType) {
        Long enEspera = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA);
        Long atendiendo = ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.ATENDIENDO);
        Long completadosHoy = ticketRepository.countByQueueTypeAndStatusAndCreatedAtAfter(
            queueType, TicketStatus.COMPLETADO, LocalDateTime.now().with(LocalTime.MIN));
        
        return new DashboardResponse.QueueData(
            queueType.name(),
            queueType.getDisplayName(),
            enEspera.intValue(),
            atendiendo.intValue(),
            completadosHoy.intValue(),
            enEspera.intValue() * queueType.getAvgTimeMinutes(),
            queueType.getAvgTimeMinutes(),
            enEspera > 10 ? "ALTA_DEMANDA" : "NORMAL"
        );
    }

    private List<DashboardResponse.AlertData> generarAlerts() {
        // Alertas básicas del sistema
        return List.of();
    }
}
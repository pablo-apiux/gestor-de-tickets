package com.example.ticketero.service;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.repository.AdvisorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de asesores/ejecutivos
 * Maneja estados, asignaciones y disponibilidad
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdvisorService {

    private final AdvisorRepository advisorRepository;

    /**
     * Obtiene asesores disponibles ordenados por carga de trabajo
     * @return Lista de asesores disponibles
     */
    @Transactional(readOnly = true)
    public List<Advisor> obtenerAsesoresDisponibles() {
        log.debug("Obteniendo asesores disponibles");
        List<Advisor> asesores = advisorRepository.findByStatusOrderByAssignedTicketsCountAscUpdatedAtAsc(AdvisorStatus.AVAILABLE);
        log.debug("Encontrados {} asesores disponibles", asesores.size());
        return asesores;
    }

    /**
     * Obtiene el asesor disponible con menor carga de trabajo
     * @return Optional del asesor con menos tickets asignados
     */
    @Transactional(readOnly = true)
    public Optional<Advisor> obtenerAsesorConMenosTickets() {
        Optional<Advisor> advisor = advisorRepository.findFirstByStatusOrderByAssignedTicketsCountAsc(AdvisorStatus.AVAILABLE);
        if (advisor.isPresent()) {
            log.debug("Asesor con menos tickets: {} ({})", advisor.get().getName(), advisor.get().getAssignedTicketsCount());
        } else {
            log.warn("No hay asesores disponibles");
        }
        return advisor;
    }

    /**
     * Cambia el estado de un asesor
     * @param advisorId ID del asesor
     * @param nuevoEstado Nuevo estado a asignar
     */
    public void cambiarEstadoAsesor(Long advisorId, AdvisorStatus nuevoEstado) {
        if (advisorId == null || nuevoEstado == null) {
            throw new IllegalArgumentException("ID del asesor y nuevo estado no pueden ser null");
        }
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new IllegalArgumentException("Asesor no encontrado con ID: " + advisorId));
        
        AdvisorStatus estadoAnterior = advisor.getStatus();
        advisor.setStatus(nuevoEstado);
        advisorRepository.save(advisor);
        
        log.info("Estado del asesor {} cambiado de {} a {}", advisor.getName(), estadoAnterior, nuevoEstado);
    }

    /**
     * Increments the counter of tickets assigned to an advisor
     * @param advisorId ID del asesor
     */
    public void incrementarContadorTickets(Long advisorId) {
        if (advisorId == null) {
            throw new IllegalArgumentException("ID del asesor no puede ser null");
        }
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new IllegalArgumentException("Asesor no encontrado con ID: " + advisorId));
        
        int contadorAnterior = advisor.getAssignedTicketsCount();
        advisor.setAssignedTicketsCount(contadorAnterior + 1);
        advisorRepository.save(advisor);
        
        log.debug("Contador de tickets del asesor {} incrementado de {} a {}", 
            advisor.getName(), contadorAnterior, advisor.getAssignedTicketsCount());
    }

    /**
     * Obtiene todos los asesores del sistema
     * @return Lista completa de asesores
     */
    @Transactional(readOnly = true)
    public List<Advisor> obtenerTodosLosAsesores() {
        log.debug("Obteniendo todos los asesores");
        return advisorRepository.findAll();
    }
}
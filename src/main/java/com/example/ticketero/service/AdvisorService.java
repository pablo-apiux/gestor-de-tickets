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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdvisorService {

    private final AdvisorRepository advisorRepository;

    @Transactional(readOnly = true)
    public List<Advisor> obtenerAsesoresDisponibles() {
        return advisorRepository.findByStatusOrderByAssignedTicketsCountAscUpdatedAtAsc(AdvisorStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public Optional<Advisor> obtenerAsesorConMenosTickets() {
        return advisorRepository.findFirstByStatusOrderByAssignedTicketsCountAsc(AdvisorStatus.AVAILABLE);
    }

    public void cambiarEstadoAsesor(Long advisorId, AdvisorStatus nuevoEstado) {
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new RuntimeException("Asesor no encontrado"));
        
        advisor.setStatus(nuevoEstado);
        advisorRepository.save(advisor);
        
        log.info("Estado del asesor {} cambiado a {}", advisor.getName(), nuevoEstado);
    }

    public void incrementarContadorTickets(Long advisorId) {
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new RuntimeException("Asesor no encontrado"));
        
        advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() + 1);
        advisorRepository.save(advisor);
    }

    @Transactional(readOnly = true)
    public List<Advisor> obtenerTodosLosAsesores() {
        return advisorRepository.findAll();
    }
}
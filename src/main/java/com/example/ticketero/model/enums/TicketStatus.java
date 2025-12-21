package com.example.ticketero.model.enums;

import java.util.List;

/**
 * Estados posibles de un ticket
 */
public enum TicketStatus {
    EN_ESPERA,      // Esperando asignación
    PROXIMO,        // Próximo a ser atendido (posición <= 3)
    ATENDIENDO,     // Siendo atendido por un asesor
    COMPLETADO,     // Atención finalizada
    CANCELADO,      // Cancelado por cliente o sistema
    NO_ATENDIDO;    // Cliente no se presentó

    /** Estados considerados "activos" (cliente aún no ha sido atendido completamente) */
    private static final List<TicketStatus> ACTIVE_STATUSES = List.of(EN_ESPERA, PROXIMO, ATENDIENDO);
    
    /** Estados finales (no requieren más procesamiento) */
    private static final List<TicketStatus> FINAL_STATUSES = List.of(COMPLETADO, CANCELADO, NO_ATENDIDO);

    /**
     * Estados considerados "activos" (cliente aún no ha sido atendido completamente)
     */
    public static List<TicketStatus> getActiveStatuses() {
        return ACTIVE_STATUSES;
    }
    
    /**
     * Estados finales que no requieren más procesamiento
     */
    public static List<TicketStatus> getFinalStatuses() {
        return FINAL_STATUSES;
    }

    /**
     * Verifica si este estado es considerado activo
     */
    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }
    
    /**
     * Verifica si este estado es final
     */
    public boolean isFinal() {
        return FINAL_STATUSES.contains(this);
    }
}
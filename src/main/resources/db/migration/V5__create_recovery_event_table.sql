-- V5__create_recovery_event_table.sql
-- Tabla para eventos de recuperación del sistema

CREATE TABLE recovery_event (
    id BIGSERIAL PRIMARY KEY,
    recovery_type VARCHAR(50) NOT NULL,
    advisor_id BIGINT,
    ticket_id BIGINT,
    old_advisor_status VARCHAR(20),
    new_advisor_status VARCHAR(20),
    old_ticket_status VARCHAR(20),
    new_ticket_status VARCHAR(20),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_recovery_advisor 
        FOREIGN KEY (advisor_id) 
        REFERENCES advisor(id) 
        ON DELETE SET NULL,
        
    CONSTRAINT fk_recovery_ticket 
        FOREIGN KEY (ticket_id) 
        REFERENCES ticket(id) 
        ON DELETE SET NULL
);

-- Índices para auditoría y consultas
CREATE INDEX idx_recovery_type ON recovery_event(recovery_type);
CREATE INDEX idx_recovery_advisor_id ON recovery_event(advisor_id);
CREATE INDEX idx_recovery_ticket_id ON recovery_event(ticket_id);
CREATE INDEX idx_recovery_created_at ON recovery_event(created_at DESC);

-- Comentarios
COMMENT ON TABLE recovery_event IS 'Eventos de recuperación y auditoría del sistema';
COMMENT ON COLUMN recovery_event.recovery_type IS 'Tipo de recuperación: ADVISOR_TIMEOUT, TICKET_REASSIGN, etc.';
COMMENT ON COLUMN recovery_event.reason IS 'Descripción detallada del motivo de la recuperación';
-- V4__create_outbox_message_table.sql
-- Tabla para patrón Outbox (Event Sourcing)

CREATE TABLE outbox_message (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_outbox_status ON outbox_message(status);
CREATE INDEX idx_outbox_created_at ON outbox_message(created_at);
CREATE INDEX idx_outbox_next_retry ON outbox_message(next_retry_at) WHERE next_retry_at IS NOT NULL;

-- Comentarios
COMMENT ON TABLE outbox_message IS 'Mensajes del patrón Outbox para garantizar consistencia eventual';
COMMENT ON COLUMN outbox_message.aggregate_type IS 'Tipo de agregado (TICKET, ADVISOR, etc.)';
COMMENT ON COLUMN outbox_message.event_type IS 'Tipo de evento (TICKET_CREATED, TICKET_CALLED, etc.)';
COMMENT ON COLUMN outbox_message.status IS 'Estado: PENDING, PROCESSED, FAILED';
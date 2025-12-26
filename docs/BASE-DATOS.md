# 🗄️ Documentación de Base de Datos - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Información General](#información-general)
2. [Diagrama de Entidad-Relación](#diagrama-de-entidad-relación)
3. [Descripción de Tablas](#descripción-de-tablas)
4. [Relaciones entre Tablas](#relaciones-entre-tablas)
5. [Índices y Restricciones](#índices-y-restricciones)
6. [Migraciones de Flyway](#migraciones-de-flyway)
7. [Consultas SQL Comunes](#consultas-sql-comunes)
8. [Estrategias de Backup y Recuperación](#estrategias-de-backup-y-recuperación)
9. [Optimización y Performance](#optimización-y-performance)

---

## 🌐 Información General

### Motor de Base de Datos
- **PostgreSQL 16**
- **Charset**: UTF-8
- **Timezone**: UTC
- **Puerto**: 5432

### Configuración de Conexión
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ticketero
    username: dev
    password: dev123
    driver-class-name: org.postgresql.Driver
```

### Herramientas de Migración
- **Flyway**: Gestión de migraciones de esquema
- **JPA/Hibernate**: ORM para mapeo objeto-relacional
- **Validación**: `ddl-auto: validate`

---

## 🔗 Diagrama de Entidad-Relación

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     TICKET      │       │     ADVISOR     │       │     MENSAJE     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │   ┌───│ id (PK)         │       │ id (PK)         │
│ codigo_ref (UK) │   │   │ name            │       │ ticket_id (FK)  │──┐
│ numero (UK)     │   │   │ email (UK)      │       │ plantilla       │  │
│ national_id     │   │   │ status          │       │ estado_envio    │  │
│ telefono        │   │   │ module_number   │       │ fecha_program   │  │
│ branch_office   │   │   │ tickets_count   │       │ fecha_envio     │  │
│ queue_type      │   │   │ created_at      │       │ telegram_msg_id │  │
│ status          │   │   │ updated_at      │       │ intentos        │  │
│ position_queue  │   │   └─────────────────┘       │ created_at      │  │
│ estimated_wait  │   │                             └─────────────────┘  │
│ advisor_id (FK) │───┘                                      │           │
│ module_number   │                                          │           │
│ created_at      │                                          │           │
│ updated_at      │                                          │           │
└─────────────────┘                                          │           │
         │                                                   │           │
         └───────────────────────────────────────────────────┘           │
                                                                         │
┌─────────────────┐       ┌─────────────────┐                          │
│ OUTBOX_MESSAGE  │       │ RECOVERY_EVENT  │                          │
├─────────────────┤       ├─────────────────┤                          │
│ id (PK)         │       │ id (PK)         │                          │
│ aggregate_type  │       │ recovery_type   │                          │
│ aggregate_id    │       │ advisor_id (FK) │──────────────────────────┘
│ event_type      │       │ ticket_id (FK)  │
│ payload         │       │ old_adv_status  │
│ routing_key     │       │ new_adv_status  │
│ status          │       │ old_tkt_status  │
│ retry_count     │       │ new_tkt_status  │
│ max_retries     │       │ reason          │
│ next_retry_at   │       │ created_at      │
│ error_message   │       └─────────────────┘
│ created_at      │
│ processed_at    │
└─────────────────┘
```

---

## 📊 Descripción de Tablas

### 1. TICKET - Tabla Principal de Tickets

**Propósito**: Almacena todos los tickets de atención creados en el sistema.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único interno |
| `codigo_referencia` | UUID | NOT NULL, UNIQUE | UUID para referencias externas |
| `numero` | VARCHAR(10) | NOT NULL, UNIQUE | Número visible (C001, P015, etc.) |
| `national_id` | VARCHAR(20) | NOT NULL | RUT/Cédula del cliente |
| `telefono` | VARCHAR(20) | NULL | Teléfono de contacto (+56XXXXXXXXX) |
| `branch_office` | VARCHAR(100) | NOT NULL | Nombre de la sucursal |
| `queue_type` | VARCHAR(20) | NOT NULL | Tipo de cola (CAJA, PERSONAL_BANKER, etc.) |
| `status` | VARCHAR(20) | NOT NULL | Estado del ticket |
| `position_in_queue` | INTEGER | NOT NULL | Posición actual en la cola |
| `estimated_wait_minutes` | INTEGER | NOT NULL | Tiempo estimado de espera |
| `assigned_advisor_id` | BIGINT | NULL, FK | ID del asesor asignado |
| `assigned_module_number` | INTEGER | NULL | Número de módulo asignado |
| `created_at` | TIMESTAMP | NOT NULL | Fecha de creación |
| `updated_at` | TIMESTAMP | NOT NULL | Fecha de última actualización |

**Estados Posibles**:
- `EN_ESPERA`: Esperando asignación
- `PROXIMO`: Próximo a ser atendido
- `ATENDIENDO`: Siendo atendido
- `COMPLETADO`: Atención finalizada
- `CANCELADO`: Cancelado
- `NO_ATENDIDO`: Cliente no se presentó

---

### 2. ADVISOR - Tabla de Asesores

**Propósito**: Gestiona los asesores/ejecutivos que atienden a los clientes.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `name` | VARCHAR(100) | NOT NULL | Nombre completo del asesor |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | Email corporativo |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'AVAILABLE' | Estado actual |
| `module_number` | INTEGER | NOT NULL, CHECK (1-5) | Número de módulo asignado |
| `assigned_tickets_count` | INTEGER | NOT NULL, DEFAULT 0, CHECK (>=0) | Contador de tickets asignados |
| `created_at` | TIMESTAMP | NOT NULL | Fecha de creación |
| `updated_at` | TIMESTAMP | NOT NULL | Fecha de actualización |

**Estados Posibles**:
- `AVAILABLE`: Disponible para atender
- `BUSY`: Atendiendo un cliente
- `OFFLINE`: No disponible

**Datos Iniciales**: 5 asesores con módulos del 1 al 5.

---

### 3. MENSAJE - Tabla de Notificaciones

**Propósito**: Gestiona las notificaciones programadas para Telegram.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `ticket_id` | BIGINT | NOT NULL, FK | Referencia al ticket |
| `plantilla` | VARCHAR(50) | NOT NULL | Tipo de mensaje |
| `estado_envio` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDIENTE' | Estado del envío |
| `fecha_programada` | TIMESTAMP | NOT NULL | Cuándo debe enviarse |
| `fecha_envio` | TIMESTAMP | NULL | Cuándo se envió realmente |
| `telegram_message_id` | VARCHAR(50) | NULL | ID del mensaje en Telegram |
| `intentos` | INTEGER | NOT NULL, DEFAULT 0 | Número de reintentos |
| `created_at` | TIMESTAMP | NOT NULL | Fecha de creación |

**Tipos de Plantilla**:
- `totem_ticket_creado`: Notificación de ticket creado
- `totem_proximo_turno`: Aviso de próximo turno
- `totem_es_tu_turno`: Llamada a atención

**Estados de Envío**:
- `PENDIENTE`: Esperando envío
- `ENVIADO`: Enviado exitosamente
- `FALLIDO`: Error en el envío

---

### 4. OUTBOX_MESSAGE - Patrón Outbox

**Propósito**: Implementa el patrón Outbox para garantizar consistencia eventual en mensajería.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `aggregate_type` | VARCHAR(50) | NOT NULL | Tipo de agregado (TICKET, ADVISOR) |
| `aggregate_id` | BIGINT | NOT NULL | ID del agregado |
| `event_type` | VARCHAR(100) | NOT NULL | Tipo de evento |
| `payload` | TEXT | NOT NULL | Datos del evento en JSON |
| `routing_key` | VARCHAR(100) | NOT NULL | Clave de enrutamiento RabbitMQ |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Estado del mensaje |
| `retry_count` | INTEGER | NOT NULL, DEFAULT 0 | Número de reintentos |
| `max_retries` | INTEGER | NOT NULL, DEFAULT 5 | Máximo de reintentos |
| `next_retry_at` | TIMESTAMP | NULL | Próximo intento programado |
| `error_message` | TEXT | NULL | Mensaje de error si falla |
| `created_at` | TIMESTAMP | NOT NULL | Fecha de creación |
| `processed_at` | TIMESTAMP | NULL | Fecha de procesamiento |

**Tipos de Evento**:
- `TICKET_CREATED`: Ticket creado
- `TICKET_CALLED`: Ticket llamado
- `TICKET_COMPLETED`: Ticket completado
- `ADVISOR_STATUS_CHANGED`: Estado de asesor cambiado

---

### 5. RECOVERY_EVENT - Eventos de Recuperación

**Propósito**: Auditoría y recuperación automática del sistema.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `recovery_type` | VARCHAR(50) | NOT NULL | Tipo de recuperación |
| `advisor_id` | BIGINT | NULL, FK | ID del asesor afectado |
| `ticket_id` | BIGINT | NULL, FK | ID del ticket afectado |
| `old_advisor_status` | VARCHAR(20) | NULL | Estado anterior del asesor |
| `new_advisor_status` | VARCHAR(20) | NULL | Nuevo estado del asesor |
| `old_ticket_status` | VARCHAR(20) | NULL | Estado anterior del ticket |
| `new_ticket_status` | VARCHAR(20) | NULL | Nuevo estado del ticket |
| `reason` | TEXT | NULL | Descripción del motivo |
| `created_at` | TIMESTAMP | NOT NULL | Fecha del evento |

**Tipos de Recuperación**:
- `ADVISOR_TIMEOUT`: Asesor no responde
- `TICKET_REASSIGN`: Reasignación de ticket
- `SYSTEM_RECOVERY`: Recuperación del sistema

---

## 🔗 Relaciones entre Tablas

### Relaciones Principales

#### 1. TICKET ↔ ADVISOR (Many-to-One)
```sql
ALTER TABLE ticket
ADD CONSTRAINT fk_ticket_advisor 
FOREIGN KEY (assigned_advisor_id) 
REFERENCES advisor(id) 
ON DELETE SET NULL;
```
- Un ticket puede tener un asesor asignado
- Un asesor puede tener múltiples tickets asignados
- Si se elimina un asesor, los tickets quedan sin asignar

#### 2. MENSAJE ↔ TICKET (Many-to-One)
```sql
ALTER TABLE mensaje
ADD CONSTRAINT fk_mensaje_ticket 
FOREIGN KEY (ticket_id) 
REFERENCES ticket(id) 
ON DELETE CASCADE;
```
- Un ticket puede tener múltiples mensajes
- Si se elimina un ticket, se eliminan sus mensajes

#### 3. RECOVERY_EVENT ↔ ADVISOR (Many-to-One)
```sql
ALTER TABLE recovery_event
ADD CONSTRAINT fk_recovery_advisor 
FOREIGN KEY (advisor_id) 
REFERENCES advisor(id) 
ON DELETE SET NULL;
```

#### 4. RECOVERY_EVENT ↔ TICKET (Many-to-One)
```sql
ALTER TABLE recovery_event
ADD CONSTRAINT fk_recovery_ticket 
FOREIGN KEY (ticket_id) 
REFERENCES ticket(id) 
ON DELETE SET NULL;
```

---

## 📇 Índices y Restricciones

### Índices de Performance

#### Tabla TICKET
```sql
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
CREATE INDEX idx_ticket_queue_type ON ticket(queue_type);
CREATE INDEX idx_ticket_created_at ON ticket(created_at DESC);
```

#### Tabla ADVISOR
```sql
CREATE INDEX idx_advisor_status ON advisor(status);
CREATE INDEX idx_advisor_module ON advisor(module_number);
```

#### Tabla MENSAJE
```sql
CREATE INDEX idx_mensaje_estado_fecha ON mensaje(estado_envio, fecha_programada);
CREATE INDEX idx_mensaje_ticket_id ON mensaje(ticket_id);
```

#### Tabla OUTBOX_MESSAGE
```sql
CREATE INDEX idx_outbox_status ON outbox_message(status);
CREATE INDEX idx_outbox_created_at ON outbox_message(created_at);
CREATE INDEX idx_outbox_next_retry ON outbox_message(next_retry_at) 
WHERE next_retry_at IS NOT NULL;
```

#### Tabla RECOVERY_EVENT
```sql
CREATE INDEX idx_recovery_type ON recovery_event(recovery_type);
CREATE INDEX idx_recovery_advisor_id ON recovery_event(advisor_id);
CREATE INDEX idx_recovery_ticket_id ON recovery_event(ticket_id);
CREATE INDEX idx_recovery_created_at ON recovery_event(created_at DESC);
```

### Restricciones de Integridad

#### Check Constraints
```sql
-- ADVISOR: Módulo entre 1 y 5
ALTER TABLE advisor 
ADD CONSTRAINT chk_module_number 
CHECK (module_number BETWEEN 1 AND 5);

-- ADVISOR: Contador no negativo
ALTER TABLE advisor 
ADD CONSTRAINT chk_assigned_count 
CHECK (assigned_tickets_count >= 0);
```

#### Unique Constraints
```sql
-- TICKET: Código de referencia único
ALTER TABLE ticket 
ADD CONSTRAINT uk_ticket_codigo_referencia 
UNIQUE (codigo_referencia);

-- TICKET: Número único
ALTER TABLE ticket 
ADD CONSTRAINT uk_ticket_numero 
UNIQUE (numero);

-- ADVISOR: Email único
ALTER TABLE advisor 
ADD CONSTRAINT uk_advisor_email 
UNIQUE (email);
```

---

## 🔄 Migraciones de Flyway

### Orden de Ejecución

| Versión | Archivo | Descripción |
|---------|---------|-------------|
| V1 | `V1__create_ticket_table.sql` | Tabla principal de tickets |
| V2 | `V2__create_mensaje_table.sql` | Tabla de notificaciones |
| V3 | `V3__create_advisor_table.sql` | Tabla de asesores + FK |
| V4 | `V4__create_outbox_message_table.sql` | Patrón Outbox |
| V5 | `V5__create_recovery_event_table.sql` | Eventos de recuperación |

### Comandos de Flyway

```bash
# Información del estado
mvn flyway:info

# Ejecutar migraciones pendientes
mvn flyway:migrate

# Validar migraciones
mvn flyway:validate

# Limpiar base de datos (solo desarrollo)
mvn flyway:clean
```

### Configuración
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

---

## 🔍 Consultas SQL Comunes

### Consultas de Tickets

#### Tickets Activos por Cola
```sql
SELECT 
    queue_type,
    COUNT(*) as total_tickets,
    COUNT(*) FILTER (WHERE status = 'EN_ESPERA') as waiting,
    COUNT(*) FILTER (WHERE status = 'ATENDIENDO') as attending
FROM ticket 
WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')
GROUP BY queue_type
ORDER BY queue_type;
```

#### Próximos Tickets a Llamar
```sql
SELECT 
    t.numero,
    t.national_id,
    t.queue_type,
    t.position_in_queue,
    t.estimated_wait_minutes,
    t.created_at
FROM ticket t
WHERE t.status = 'EN_ESPERA'
ORDER BY t.queue_type, t.position_in_queue
LIMIT 10;
```

#### Tickets por Asesor
```sql
SELECT 
    a.name as asesor,
    a.module_number,
    COUNT(t.id) as tickets_asignados,
    COUNT(t.id) FILTER (WHERE t.status = 'ATENDIENDO') as atendiendo_ahora
FROM advisor a
LEFT JOIN ticket t ON a.id = t.assigned_advisor_id 
    AND t.status IN ('ATENDIENDO', 'COMPLETADO')
    AND DATE(t.created_at) = CURRENT_DATE
GROUP BY a.id, a.name, a.module_number
ORDER BY tickets_asignados DESC;
```

### Consultas de Dashboard

#### Métricas del Día
```sql
SELECT 
    COUNT(*) as total_tickets_hoy,
    COUNT(*) FILTER (WHERE status = 'EN_ESPERA') as esperando,
    COUNT(*) FILTER (WHERE status = 'ATENDIENDO') as atendiendo,
    COUNT(*) FILTER (WHERE status = 'COMPLETADO') as completados,
    AVG(estimated_wait_minutes) as tiempo_promedio_espera
FROM ticket 
WHERE DATE(created_at) = CURRENT_DATE;
```

#### Estado de Asesores
```sql
SELECT 
    status,
    COUNT(*) as cantidad
FROM advisor 
GROUP BY status
ORDER BY 
    CASE status 
        WHEN 'AVAILABLE' THEN 1 
        WHEN 'BUSY' THEN 2 
        WHEN 'OFFLINE' THEN 3 
    END;
```

### Consultas de Notificaciones

#### Mensajes Pendientes
```sql
SELECT 
    m.id,
    m.plantilla,
    t.numero,
    m.fecha_programada,
    m.intentos
FROM mensaje m
JOIN ticket t ON m.ticket_id = t.id
WHERE m.estado_envio = 'PENDIENTE'
    AND m.fecha_programada <= NOW()
ORDER BY m.fecha_programada;
```

#### Mensajes Fallidos para Reintento
```sql
SELECT 
    m.id,
    m.plantilla,
    t.numero,
    m.intentos,
    m.fecha_programada
FROM mensaje m
JOIN ticket t ON m.ticket_id = t.id
WHERE m.estado_envio = 'FALLIDO'
    AND m.intentos < 3
    AND m.fecha_programada <= NOW() - INTERVAL '5 minutes';
```

### Consultas de Auditoría

#### Eventos de Recuperación Recientes
```sql
SELECT 
    re.recovery_type,
    re.reason,
    a.name as asesor,
    t.numero as ticket,
    re.created_at
FROM recovery_event re
LEFT JOIN advisor a ON re.advisor_id = a.id
LEFT JOIN ticket t ON re.ticket_id = t.id
WHERE re.created_at >= NOW() - INTERVAL '24 hours'
ORDER BY re.created_at DESC;
```

#### Mensajes Outbox Pendientes
```sql
SELECT 
    aggregate_type,
    event_type,
    status,
    retry_count,
    created_at,
    next_retry_at
FROM outbox_message 
WHERE status = 'PENDING'
    OR (status = 'FAILED' AND retry_count < max_retries)
ORDER BY created_at;
```

---

## 💾 Estrategias de Backup y Recuperación

### Backup Automático

#### Script de Backup Diario
```bash
#!/bin/bash
# backup-ticketero.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/ticketero"
DB_NAME="ticketero"
DB_USER="dev"

# Crear directorio si no existe
mkdir -p $BACKUP_DIR

# Backup completo
pg_dump -h localhost -U $DB_USER -d $DB_NAME \
    --verbose --clean --no-owner --no-privileges \
    --file="$BACKUP_DIR/ticketero_full_$DATE.sql"

# Comprimir backup
gzip "$BACKUP_DIR/ticketero_full_$DATE.sql"

# Limpiar backups antiguos (mantener 7 días)
find $BACKUP_DIR -name "ticketero_full_*.sql.gz" -mtime +7 -delete

echo "Backup completado: ticketero_full_$DATE.sql.gz"
```

#### Backup Solo de Datos
```bash
# Solo datos (sin esquema)
pg_dump -h localhost -U dev -d ticketero \
    --data-only --verbose \
    --file="ticketero_data_$(date +%Y%m%d).sql"
```

### Restauración

#### Restauración Completa
```bash
# Restaurar desde backup completo
psql -h localhost -U dev -d ticketero_new < ticketero_full_20240115.sql
```

#### Restauración Solo de Datos
```bash
# Restaurar solo datos (esquema debe existir)
psql -h localhost -U dev -d ticketero < ticketero_data_20240115.sql
```

### Estrategia de Recuperación ante Desastres

#### 1. Backup Incremental
```sql
-- Backup de cambios desde última fecha
SELECT * FROM ticket 
WHERE updated_at > '2024-01-15 00:00:00';

SELECT * FROM mensaje 
WHERE created_at > '2024-01-15 00:00:00';
```

#### 2. Replicación
```yaml
# Configuración para réplica de lectura
spring:
  datasource:
    primary:
      url: jdbc:postgresql://primary:5432/ticketero
    replica:
      url: jdbc:postgresql://replica:5432/ticketero
```

#### 3. Point-in-Time Recovery
```bash
# Habilitar WAL archiving en PostgreSQL
archive_mode = on
archive_command = 'cp %p /archive/%f'
wal_level = replica
```

---

## ⚡ Optimización y Performance

### Monitoreo de Performance

#### Consultas Lentas
```sql
-- Habilitar log de consultas lentas
ALTER SYSTEM SET log_min_duration_statement = 1000; -- 1 segundo
SELECT pg_reload_conf();

-- Ver consultas más lentas
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements 
ORDER BY total_time DESC 
LIMIT 10;
```

#### Estadísticas de Tablas
```sql
-- Estadísticas de uso de tablas
SELECT 
    schemaname,
    tablename,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes,
    seq_scan,
    idx_scan
FROM pg_stat_user_tables 
ORDER BY seq_scan DESC;
```

### Optimizaciones Recomendadas

#### 1. Particionamiento de Tablas
```sql
-- Particionar tabla ticket por fecha (mensual)
CREATE TABLE ticket_2024_01 PARTITION OF ticket
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE ticket_2024_02 PARTITION OF ticket
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

#### 2. Índices Compuestos
```sql
-- Índice compuesto para consultas frecuentes
CREATE INDEX idx_ticket_queue_status_created 
ON ticket(queue_type, status, created_at DESC);

-- Índice parcial para tickets activos
CREATE INDEX idx_ticket_active 
ON ticket(queue_type, position_in_queue) 
WHERE status IN ('EN_ESPERA', 'PROXIMO');
```

#### 3. Mantenimiento Automático
```sql
-- Configurar autovacuum más agresivo para tablas activas
ALTER TABLE ticket SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
);

ALTER TABLE mensaje SET (
    autovacuum_vacuum_scale_factor = 0.2,
    autovacuum_analyze_scale_factor = 0.1
);
```

### Limpieza de Datos

#### Script de Limpieza Semanal
```sql
-- Limpiar tickets completados antiguos (más de 30 días)
DELETE FROM ticket 
WHERE status = 'COMPLETADO' 
    AND updated_at < NOW() - INTERVAL '30 days';

-- Limpiar mensajes enviados antiguos (más de 7 días)
DELETE FROM mensaje 
WHERE estado_envio = 'ENVIADO' 
    AND fecha_envio < NOW() - INTERVAL '7 days';

-- Limpiar mensajes outbox procesados (más de 24 horas)
DELETE FROM outbox_message 
WHERE status = 'PROCESSED' 
    AND processed_at < NOW() - INTERVAL '24 hours';

-- Actualizar estadísticas
ANALYZE;
```

---

## 📊 Métricas y Monitoreo

### Consultas de Monitoreo

#### Tamaño de Tablas
```sql
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(tablename::regclass)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(tablename::regclass) DESC;
```

#### Conexiones Activas
```sql
SELECT 
    state,
    COUNT(*) as connections
FROM pg_stat_activity 
WHERE datname = 'ticketero'
GROUP BY state;
```

#### Locks Activos
```sql
SELECT 
    mode,
    locktype,
    COUNT(*) as locks
FROM pg_locks 
GROUP BY mode, locktype
ORDER BY locks DESC;
```

---

**📞 Soporte**: Para problemas con la base de datos, consultar la [Guía de Troubleshooting](TROUBLESHOOTING.md) o revisar los logs de PostgreSQL.
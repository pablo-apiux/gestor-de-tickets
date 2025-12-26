# 🔧 Guía de Troubleshooting - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Problemas de Conexión a Base de Datos](#problemas-de-conexión-a-base-de-datos)
2. [Errores de RabbitMQ y Mensajería](#errores-de-rabbitmq-y-mensajería)
3. [Fallos en Notificaciones de Telegram](#fallos-en-notificaciones-de-telegram)
4. [Problemas de Rendimiento](#problemas-de-rendimiento)
5. [Errores de Validación Comunes](#errores-de-validación-comunes)
6. [Logs y Monitoreo](#logs-y-monitoreo)
7. [Herramientas de Diagnóstico](#herramientas-de-diagnóstico)
8. [Procedimientos de Recuperación](#procedimientos-de-recuperación)

---

## 🗄️ Problemas de Conexión a Base de Datos

### Error: "Connection refused" a PostgreSQL

#### Síntomas
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

#### Causas Posibles
1. PostgreSQL no está ejecutándose
2. Puerto 5432 ocupado por otro proceso
3. Configuración incorrecta de conexión
4. Firewall bloqueando conexión

#### Soluciones

##### 1. Verificar Estado de PostgreSQL
```bash
# Verificar si PostgreSQL está corriendo
docker ps | grep postgres

# Si no está corriendo, iniciarlo
docker-compose up postgres -d

# Verificar logs
docker-compose logs postgres
```

##### 2. Verificar Puerto
```bash
# Windows
netstat -ano | findstr :5432

# Linux/macOS
lsof -i :5432
```

##### 3. Verificar Configuración
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ticketero
    username: dev
    password: dev123
```

##### 4. Reiniciar Servicios
```bash
# Reiniciar PostgreSQL
docker-compose restart postgres

# Reiniciar toda la aplicación
docker-compose down && docker-compose up -d
```

### Error: "Authentication failed"

#### Síntomas
```
org.postgresql.util.PSQLException: FATAL: password authentication failed for user "dev"
```

#### Soluciones
```bash
# Verificar credenciales en docker-compose.yml
POSTGRES_USER: dev
POSTGRES_PASSWORD: dev123

# Recrear contenedor con credenciales correctas
docker-compose down -v
docker-compose up postgres -d
```

### Error: "Database does not exist"

#### Síntomas
```
org.postgresql.util.PSQLException: FATAL: database "ticketero" does not exist
```

#### Soluciones
```bash
# Crear base de datos manualmente
docker exec -it ticketero-db psql -U dev -c "CREATE DATABASE ticketero;"

# O recrear contenedor
docker-compose down -v
docker-compose up postgres -d
```

### Error: "Too many connections"

#### Síntomas
```
org.postgresql.util.PSQLException: FATAL: too many connections for role "dev"
```

#### Soluciones
```yaml
# Ajustar pool de conexiones en application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
```

```sql
-- Verificar conexiones activas
SELECT count(*) FROM pg_stat_activity WHERE datname = 'ticketero';

-- Terminar conexiones inactivas
SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE datname = 'ticketero' AND state = 'idle';
```

---

## 🐰 Errores de RabbitMQ y Mensajería

### Error: "Connection refused" a RabbitMQ

#### Síntomas
```
java.net.ConnectException: Connection refused: no further information
```

#### Soluciones

##### 1. Verificar Estado de RabbitMQ
```bash
# Verificar contenedor
docker ps | grep rabbitmq

# Iniciar RabbitMQ
docker-compose up rabbitmq -d

# Verificar logs
docker-compose logs rabbitmq
```

##### 2. Verificar Configuración
```yaml
# application.yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: dev
    password: dev123
```

##### 3. Verificar Management UI
- URL: http://localhost:15672
- Usuario: dev
- Contraseña: dev123

### Error: "Queue not found"

#### Síntomas
```
com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>
```

#### Soluciones
```bash
# Verificar colas existentes en Management UI
# O crear colas manualmente si es necesario

# Reiniciar RabbitMQ para recrear colas
docker-compose restart rabbitmq
```

### Error: "Outbox messages not processing"

#### Síntomas
- Mensajes quedan en estado PENDING
- No se procesan eventos del patrón Outbox

#### Diagnóstico
```sql
-- Verificar mensajes pendientes
SELECT status, COUNT(*) FROM outbox_message GROUP BY status;

-- Ver mensajes con errores
SELECT * FROM outbox_message 
WHERE status = 'FAILED' 
ORDER BY created_at DESC 
LIMIT 10;
```

#### Soluciones
```bash
# Verificar que el scheduler esté habilitado
# En TicketeroApplication.java debe tener @EnableScheduling

# Verificar logs del OutboxPublisherService
docker-compose logs api | grep -i outbox

# Reiniciar aplicación
docker-compose restart api
```

### Error: "Message serialization failed"

#### Síntomas
```
com.fasterxml.jackson.core.JsonProcessingException: Cannot serialize object
```

#### Soluciones
```java
// Verificar que los objetos en payload sean serializables
// Evitar referencias circulares en entidades JPA

@JsonIgnore
private List<Ticket> assignedTickets; // En Advisor entity
```

---

## 📱 Fallos en Notificaciones de Telegram

### Error: "Bot token invalid"

#### Síntomas
```
Telegram API error: 401 Unauthorized - Bot token invalid
```

#### Soluciones

##### 1. Verificar Token
```bash
# Verificar variables de entorno
echo $TELEGRAM_BOT_TOKEN

# Verificar en archivo .env
cat .env | grep TELEGRAM_BOT_TOKEN
```

##### 2. Validar Token con API
```bash
# Probar token manualmente
curl "https://api.telegram.org/bot<TOKEN>/getMe"
```

##### 3. Regenerar Token
1. Contactar @BotFather en Telegram
2. Usar comando `/token`
3. Actualizar variable de entorno
4. Reiniciar aplicación

### Error: "Chat not found"

#### Síntomas
```
Telegram API error: 400 Bad Request - Chat not found
```

#### Soluciones

##### 1. Verificar Chat ID
```bash
# Verificar TELEGRAM_CHAT_ID
echo $TELEGRAM_CHAT_ID

# Obtener chat ID correcto usando @userinfobot
```

##### 2. Verificar Permisos del Bot
- El bot debe tener permisos para enviar mensajes
- El usuario debe haber iniciado conversación con el bot

##### 3. Test de Conectividad
```bash
# Usar endpoint de debug
curl http://localhost:8090/api/debug/telegram-config

# Probar notificación
curl http://localhost:8090/api/debug/test-notification
```

### Error: "Message too long"

#### Síntomas
```
Telegram API error: 400 Bad Request - Message is too long
```

#### Soluciones
```java
// Limitar longitud de mensajes en TelegramService
private String truncateMessage(String message, int maxLength) {
    if (message.length() <= maxLength) {
        return message;
    }
    return message.substring(0, maxLength - 3) + "...";
}
```

### Error: "Rate limit exceeded"

#### Síntomas
```
Telegram API error: 429 Too Many Requests
```

#### Soluciones
```java
// Implementar rate limiting en TelegramService
@Component
public class TelegramRateLimiter {
    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 1 mensaje por segundo
    
    public void waitIfNecessary() {
        rateLimiter.acquire();
    }
}
```

### Error: "Network timeout"

#### Síntomas
```
java.net.SocketTimeoutException: Read timed out
```

#### Soluciones
```java
// Configurar timeouts en RestTemplate
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
```

---

## ⚡ Problemas de Rendimiento

### Consultas SQL Lentas

#### Síntomas
- Respuestas lentas de la API
- Timeouts en base de datos
- Alto uso de CPU en PostgreSQL

#### Diagnóstico
```sql
-- Habilitar log de consultas lentas
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- Ver consultas más lentas
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY total_time DESC 
LIMIT 10;
```

#### Soluciones

##### 1. Optimizar Consultas
```sql
-- Agregar índices faltantes
CREATE INDEX idx_ticket_status_queue ON ticket(status, queue_type);
CREATE INDEX idx_mensaje_estado_fecha ON mensaje(estado_envio, fecha_programada);

-- Analizar planes de ejecución
EXPLAIN ANALYZE SELECT * FROM ticket WHERE status = 'EN_ESPERA';
```

##### 2. Optimizar Consultas JPA
```java
// ✅ Usar consultas específicas
@Query("SELECT t FROM Ticket t WHERE t.status = :status")
List<Ticket> findByStatus(@Param("status") TicketStatus status);

// ❌ Evitar cargar todo
// List<Ticket> findAll().stream().filter(...)
```

##### 3. Configurar Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
```

### Alto Uso de Memoria

#### Síntomas
```
java.lang.OutOfMemoryError: Java heap space
```

#### Diagnóstico
```bash
# Verificar uso de memoria
docker stats ticketero-api

# Generar heap dump
docker exec ticketero-api jcmd 1 GC.run_finalization
docker exec ticketero-api jcmd 1 VM.gc
```

#### Soluciones

##### 1. Ajustar Heap Size
```dockerfile
# En Dockerfile
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]
```

##### 2. Optimizar Consultas
```java
// Usar paginación para consultas grandes
@Query("SELECT t FROM Ticket t WHERE t.status = :status")
Page<Ticket> findByStatus(@Param("status") TicketStatus status, Pageable pageable);
```

##### 3. Limpiar Datos Antiguos
```sql
-- Limpiar tickets completados antiguos
DELETE FROM ticket 
WHERE status = 'COMPLETADO' 
AND updated_at < NOW() - INTERVAL '30 days';
```

### Problemas de Concurrencia

#### Síntomas
- Deadlocks en base de datos
- Inconsistencias en posiciones de cola
- Tickets duplicados

#### Soluciones

##### 1. Usar Transacciones Apropiadas
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public TicketResponse crearTicket(TicketCreateRequest request) {
    // Lógica que requiere consistencia estricta
}
```

##### 2. Implementar Locks Optimistas
```java
@Entity
public class Ticket {
    @Version
    private Long version;
    // otros campos...
}
```

##### 3. Usar Consultas Atómicas
```sql
-- Actualizar posiciones de forma atómica
UPDATE ticket 
SET position_in_queue = position_in_queue - 1 
WHERE queue_type = 'CAJA' 
AND status = 'EN_ESPERA' 
AND position_in_queue > 1;
```

---

## ✅ Errores de Validación Comunes

### Error: "National ID is required"

#### Síntomas
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El RUT/ID es obligatorio"
}
```

#### Soluciones
```java
// Verificar que el request tenga nationalId
{
  "nationalId": "12345678-9",  // ✅ Requerido
  "telefono": "+56987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```

### Error: "Invalid phone format"

#### Síntomas
```json
{
  "message": "Teléfono debe tener formato +56XXXXXXXXX"
}
```

#### Soluciones
```java
// Formato correcto de teléfono
"telefono": "+56987654321"  // ✅ Correcto
"telefono": "987654321"     // ❌ Incorrecto
"telefono": "+569876543"    // ❌ Muy corto
```

### Error: "Queue type not supported"

#### Síntomas
```json
{
  "message": "Tipo de cola no válido"
}
```

#### Soluciones
```java
// Valores válidos para queueType
"queueType": "CAJA"           // ✅ Correcto
"queueType": "PERSONAL_BANKER" // ✅ Correcto
"queueType": "EMPRESAS"       // ✅ Correcto
"queueType": "GERENCIA"       // ✅ Correcto
"queueType": "INVALID"        // ❌ Incorrecto
```

### Error: "Ticket already exists"

#### Síntomas
```json
{
  "message": "Ya existe un ticket activo para este RUT/ID: C001"
}
```

#### Soluciones
```bash
# Verificar tickets activos del cliente
curl "http://localhost:8090/api/tickets" | grep "12345678-9"

# Finalizar ticket existente si es necesario
curl -X PUT "http://localhost:8090/api/tickets/123/finalizar"
```

---

## 📊 Logs y Monitoreo

### Configuración de Logs

#### Niveles de Log Recomendados
```yaml
logging:
  level:
    com.example.ticketero: INFO
    org.springframework: WARN
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Ubicación de Logs
```bash
# Logs de aplicación
docker-compose logs api

# Logs de base de datos
docker-compose logs postgres

# Logs de RabbitMQ
docker-compose logs rabbitmq

# Logs en tiempo real
docker-compose logs -f api
```

### Logs Importantes a Monitorear

#### 1. Creación de Tickets
```
INFO  - Creando ticket para RUT: 12345678-9
INFO  - Ticket creado exitosamente: C001
ERROR - Error creando ticket para RUT 12345678-9: Ya existe ticket activo
```

#### 2. Notificaciones de Telegram
```
INFO  - Enviando mensaje Telegram para ticket C001
INFO  - Mensaje enviado exitosamente. MessageId: 1234
ERROR - Error enviando notificación para ticket C001: Bot token invalid
```

#### 3. Procesamiento de Outbox
```
INFO  - Procesando 5 mensajes pendientes del outbox
INFO  - Mensaje procesado exitosamente: TICKET_CREATED
ERROR - Error procesando mensaje outbox: Connection refused
```

#### 4. Recuperación del Sistema
```
WARN  - Detectado asesor inactivo: ID 1
INFO  - Recuperando ticket T001 de asesor inactivo
INFO  - Ticket T001 reasignado exitosamente
```

### Métricas de Monitoreo

#### Endpoints de Actuator
```bash
# Health check
curl http://localhost:8090/actuator/health

# Métricas generales
curl http://localhost:8090/actuator/metrics

# Métricas específicas
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/hikaricp.connections.active
```

#### Consultas de Monitoreo
```sql
-- Tickets por estado
SELECT status, COUNT(*) FROM ticket GROUP BY status;

-- Mensajes outbox pendientes
SELECT status, COUNT(*) FROM outbox_message GROUP BY status;

-- Asesores por estado
SELECT status, COUNT(*) FROM advisor GROUP BY status;

-- Mensajes de Telegram fallidos
SELECT COUNT(*) FROM mensaje WHERE estado_envio = 'FALLIDO';
```

---

## 🔍 Herramientas de Diagnóstico

### Comandos de Docker

#### Verificar Estado de Contenedores
```bash
# Ver todos los contenedores
docker-compose ps

# Ver uso de recursos
docker stats

# Inspeccionar contenedor específico
docker inspect ticketero-api
```

#### Acceso a Contenedores
```bash
# Acceder a base de datos
docker exec -it ticketero-db psql -U dev -d ticketero

# Acceder a aplicación
docker exec -it ticketero-api bash

# Ver logs específicos
docker-compose logs --tail=100 api
```

### Herramientas de Base de Datos

#### Consultas de Diagnóstico
```sql
-- Conexiones activas
SELECT count(*) FROM pg_stat_activity WHERE datname = 'ticketero';

-- Tamaño de tablas
SELECT tablename, pg_size_pretty(pg_total_relation_size(tablename::regclass)) 
FROM pg_tables WHERE schemaname = 'public';

-- Locks activos
SELECT mode, locktype, COUNT(*) FROM pg_locks GROUP BY mode, locktype;

-- Estadísticas de consultas
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY total_time DESC LIMIT 5;
```

#### Herramientas Externas
```bash
# pgAdmin (interfaz web para PostgreSQL)
docker run -p 5050:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin dpage/pgadmin4

# Conectar a: localhost:5432, usuario: dev, password: dev123
```

### Herramientas de API

#### Postman Collection
```json
{
  "info": {
    "name": "Ticketero API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Ticket",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "raw": "{\n  \"nationalId\": \"12345678-9\",\n  \"telefono\": \"+56987654321\",\n  \"branchOffice\": \"Sucursal Centro\",\n  \"queueType\": \"CAJA\"\n}"
        },
        "url": "http://localhost:8090/api/tickets"
      }
    }
  ]
}
```

#### cURL Scripts
```bash
#!/bin/bash
# test-api.sh

BASE_URL="http://localhost:8090/api"

echo "Testing API health..."
curl -s "$BASE_URL/../actuator/health" | jq .

echo "Creating test ticket..."
curl -s -X POST "$BASE_URL/tickets" \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "telefono": "+56987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }' | jq .

echo "Getting active tickets..."
curl -s "$BASE_URL/tickets" | jq .
```

---

## 🚑 Procedimientos de Recuperación

### Recuperación de Base de Datos

#### Backup y Restauración
```bash
# Crear backup
docker exec ticketero-db pg_dump -U dev ticketero > backup_$(date +%Y%m%d_%H%M%S).sql

# Restaurar desde backup
docker exec -i ticketero-db psql -U dev ticketero < backup_20240115_143000.sql
```

#### Reparar Datos Corruptos
```sql
-- Recalcular posiciones en cola
WITH numbered_tickets AS (
  SELECT id, ROW_NUMBER() OVER (PARTITION BY queue_type ORDER BY created_at) as new_position
  FROM ticket 
  WHERE status = 'EN_ESPERA'
)
UPDATE ticket 
SET position_in_queue = nt.new_position,
    estimated_wait_minutes = nt.new_position * 
      CASE queue_type 
        WHEN 'CAJA' THEN 5
        WHEN 'PERSONAL_BANKER' THEN 15
        WHEN 'EMPRESAS' THEN 20
        WHEN 'GERENCIA' THEN 30
      END
FROM numbered_tickets nt 
WHERE ticket.id = nt.id;
```

### Recuperación de Servicios

#### Reinicio Completo del Sistema
```bash
#!/bin/bash
# full-restart.sh

echo "Stopping all services..."
docker-compose down

echo "Removing volumes (WARNING: This will delete data)..."
docker-compose down -v

echo "Starting services..."
docker-compose up -d

echo "Waiting for services to be ready..."
sleep 30

echo "Checking health..."
curl http://localhost:8090/actuator/health
```

#### Recuperación Parcial
```bash
# Solo reiniciar aplicación
docker-compose restart api

# Solo reiniciar base de datos
docker-compose restart postgres

# Solo reiniciar RabbitMQ
docker-compose restart rabbitmq
```

### Recuperación de Datos

#### Limpiar Mensajes Outbox Bloqueados
```sql
-- Resetear mensajes fallidos para reintento
UPDATE outbox_message 
SET status = 'PENDING', 
    retry_count = 0, 
    next_retry_at = NULL,
    error_message = NULL
WHERE status = 'FAILED' 
AND created_at > NOW() - INTERVAL '1 hour';
```

#### Liberar Asesores Bloqueados
```sql
-- Liberar asesores que quedaron en BUSY
UPDATE advisor 
SET status = 'AVAILABLE' 
WHERE status = 'BUSY' 
AND id NOT IN (
  SELECT DISTINCT assigned_advisor_id 
  FROM ticket 
  WHERE status = 'ATENDIENDO' 
  AND assigned_advisor_id IS NOT NULL
);
```

#### Reasignar Tickets Huérfanos
```sql
-- Tickets en ATENDIENDO sin asesor asignado
UPDATE ticket 
SET status = 'EN_ESPERA',
    assigned_advisor_id = NULL,
    assigned_module_number = NULL
WHERE status = 'ATENDIENDO' 
AND assigned_advisor_id IS NULL;
```

### Procedimiento de Emergencia

#### Pasos para Recuperación Completa
1. **Evaluar el Problema**
   ```bash
   # Verificar logs
   docker-compose logs --tail=50 api
   
   # Verificar estado de servicios
   docker-compose ps
   
   # Verificar conectividad
   curl http://localhost:8090/actuator/health
   ```

2. **Crear Backup de Emergencia**
   ```bash
   # Backup de base de datos
   docker exec ticketero-db pg_dump -U dev ticketero > emergency_backup.sql
   
   # Backup de logs
   docker-compose logs > emergency_logs.txt
   ```

3. **Aplicar Solución**
   ```bash
   # Reinicio suave
   docker-compose restart api
   
   # O reinicio completo si es necesario
   docker-compose down && docker-compose up -d
   ```

4. **Verificar Recuperación**
   ```bash
   # Verificar servicios
   curl http://localhost:8090/actuator/health
   
   # Probar funcionalidad crítica
   curl -X POST http://localhost:8090/api/tickets \
     -H "Content-Type: application/json" \
     -d '{"nationalId":"TEST123","branchOffice":"Test","queueType":"CAJA"}'
   ```

5. **Monitorear Post-Recuperación**
   ```bash
   # Monitorear logs en tiempo real
   docker-compose logs -f api
   
   # Verificar métricas
   curl http://localhost:8090/actuator/metrics
   ```

---

## 📞 Contacto y Escalación

### Niveles de Soporte

#### Nivel 1: Problemas Comunes
- Reinicio de servicios
- Verificación de configuración
- Consulta de logs básicos

#### Nivel 2: Problemas Técnicos
- Análisis de base de datos
- Optimización de consultas
- Recuperación de datos

#### Nivel 3: Problemas Críticos
- Corrupción de datos
- Fallos de arquitectura
- Recuperación completa del sistema

### Información para Reportar Problemas

#### Datos Requeridos
1. **Descripción del problema**
2. **Pasos para reproducir**
3. **Logs relevantes**
4. **Configuración del entorno**
5. **Hora exacta del incidente**

#### Comando de Diagnóstico Rápido
```bash
#!/bin/bash
# diagnostic-report.sh

echo "=== DIAGNOSTIC REPORT ===" > diagnostic_report.txt
echo "Date: $(date)" >> diagnostic_report.txt
echo "" >> diagnostic_report.txt

echo "=== DOCKER STATUS ===" >> diagnostic_report.txt
docker-compose ps >> diagnostic_report.txt
echo "" >> diagnostic_report.txt

echo "=== APPLICATION HEALTH ===" >> diagnostic_report.txt
curl -s http://localhost:8090/actuator/health >> diagnostic_report.txt
echo "" >> diagnostic_report.txt

echo "=== RECENT LOGS ===" >> diagnostic_report.txt
docker-compose logs --tail=50 api >> diagnostic_report.txt

echo "Diagnostic report saved to: diagnostic_report.txt"
```

---

**🚨 Emergencias**: Para problemas críticos que afecten la operación, seguir el procedimiento de recuperación completa y contactar al equipo de desarrollo con el reporte de diagnóstico.
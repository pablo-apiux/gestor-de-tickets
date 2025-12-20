# BRAINSTORM TÉCNICO - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Autor:** Arquitecto de Software Senior

---

## 1. ANÁLISIS DEL DOMINIO

### 1.1 Complejidad Real del Proyecto

**Volumen de Operaciones:**
- 25,000 tickets/día (fase nacional) = ~0.3 tickets/segundo
- 75,000 mensajes Telegram/día = ~0.9 mensajes/segundo
- 5 asesores por sucursal × 50 sucursales = 250 asesores concurrentes

**Conclusión:** Volumen BAJO-MEDIO. No requiere arquitectura distribuida compleja.

### 1.2 Características del Dominio

**Dominio Simple:**
- CRUD básico con lógica de colas FIFO
- Reglas de negocio claras y estables
- Sin complejidad algorítmica avanzada
- Transacciones simples (no hay pagos ni operaciones críticas)

**Patrones Identificados:**
- **State Machine:** Ticket (EN_ESPERA → PROXIMO → ATENDIENDO → COMPLETADO)
- **Queue Management:** Colas con prioridades y balanceo de carga
- **Event Sourcing ligero:** Auditoría de eventos
- **Scheduled Jobs:** Procesamiento asíncrono de mensajes

---

## 2. DECISIÓN ARQUITECTÓNICA: MONOLITO MODULAR

### 2.1 ¿Por qué NO Microservicios?

**Razones Técnicas:**
- Volumen insuficiente para justificar complejidad distribuida
- Transacciones simples que no requieren eventual consistency
- Equipo pequeño (3-5 desarrolladores estimado)
- Dominio cohesivo sin boundaries naturales claros

**Razones de Negocio:**
- Time-to-market crítico
- Presupuesto limitado (proyecto de capacitación)
- Mantenimiento simplificado
- Deployment atómico (menos riesgo)

### 2.2 Monolito Modular Propuesto

**Estructura por Módulos Funcionales:**

```
src/main/java/com/banco/ticketero/
├── ticket/          # Gestión de tickets (RF-001, RF-003, RF-006)
├── queue/           # Gestión de colas (RF-005)
├── advisor/         # Gestión de asesores (RF-004, RF-007)
├── notification/    # Notificaciones Telegram (RF-002)
├── audit/           # Auditoría (RF-008)
├── admin/           # Panel administrativo (RF-007)
└── shared/          # DTOs, enums, utils compartidos
```

**Ventajas:**
- Separación clara de responsabilidades
- Fácil navegación del código
- Posible extracción futura a microservicios si es necesario
- Testing independiente por módulo

---

## 3. ESTRUCTURA DEL PROYECTO SPRING BOOT 3.2

### 3.1 Estructura de Directorios

```
gestor-de-tickets/
├── src/main/java/com/banco/ticketero/
│   ├── TicketeroApplication.java                    # Main class
│   ├── config/                                      # Configuraciones
│   │   ├── DatabaseConfig.java                      # DataSource, JPA
│   │   ├── TelegramConfig.java                      # RestTemplate, Bot Token
│   │   ├── SchedulingConfig.java                    # @EnableScheduling
│   │   └── ValidationConfig.java                    # Bean Validation
│   ├── ticket/                                      # Módulo Tickets
│   │   ├── controller/TicketController.java         # REST endpoints
│   │   ├── service/TicketService.java               # Lógica de negocio
│   │   ├── repository/TicketRepository.java         # Data access
│   │   ├── entity/Ticket.java                       # JPA entity
│   │   └── dto/                                     # Request/Response DTOs
│   │       ├── TicketRequest.java
│   │       ├── TicketResponse.java
│   │       └── QueuePositionResponse.java
│   ├── queue/                                       # Módulo Colas
│   │   ├── service/QueueManagementService.java      # Asignación automática
│   │   ├── scheduler/QueueProcessorScheduler.java   # Procesamiento cada 5s
│   │   └── dto/QueueStatsResponse.java
│   ├── advisor/                                     # Módulo Asesores
│   │   ├── controller/AdvisorController.java        # Admin endpoints
│   │   ├── service/AdvisorService.java
│   │   ├── repository/AdvisorRepository.java
│   │   ├── entity/Advisor.java
│   │   └── dto/AdvisorResponse.java
│   ├── notification/                                # Módulo Notificaciones
│   │   ├── service/TelegramService.java             # Integración Telegram
│   │   ├── scheduler/MessageScheduler.java          # Envío cada 60s
│   │   ├── repository/MensajeRepository.java
│   │   ├── entity/Mensaje.java
│   │   └── template/MessageTemplateService.java     # Plantillas de mensajes
│   ├── audit/                                       # Módulo Auditoría
│   │   ├── service/AuditService.java
│   │   ├── repository/AuditLogRepository.java
│   │   ├── entity/AuditLog.java
│   │   └── aspect/AuditAspect.java                  # AOP para auditoría automática
│   ├── admin/                                       # Módulo Administración
│   │   ├── controller/AdminController.java          # Dashboard endpoints
│   │   ├── service/DashboardService.java
│   │   └── dto/DashboardResponse.java
│   └── shared/                                      # Componentes compartidos
│       ├── enums/
│       │   ├── QueueType.java
│       │   ├── TicketStatus.java
│       │   ├── AdvisorStatus.java
│       │   └── MessageTemplate.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java          # @ControllerAdvice
│       │   ├── TicketNotFoundException.java
│       │   └── BusinessRuleException.java
│       └── util/
│           ├── TicketNumberGenerator.java           # Generación de números
│           └── TimeEstimationUtil.java              # Cálculos de tiempo
├── src/main/resources/
│   ├── application.yml                              # Configuración principal
│   ├── application-dev.yml                          # Perfil desarrollo
│   ├── application-prod.yml                         # Perfil producción
│   └── db/migration/                                # Flyway migrations
│       ├── V1__create_ticket_table.sql
│       ├── V2__create_advisor_table.sql
│       ├── V3__create_mensaje_table.sql
│       ├── V4__create_audit_log_table.sql
│       └── V5__insert_initial_advisors.sql
├── src/test/java/                                   # Tests unitarios e integración
├── docker-compose.yml                               # PostgreSQL + App
├── Dockerfile                                       # Multi-stage build
└── pom.xml                                          # Maven dependencies
```

### 3.2 Configuración Maven (pom.xml)

**Dependencias Clave:**

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 4. DISEÑO DEL DOMINIO

### 4.1 Entidades Principales (JPA)

#### Ticket Entity

```java
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_referencia", unique = true, nullable = false)
    private UUID codigoReferencia;
    
    @Column(name = "numero", unique = true, nullable = false, length = 10)
    private String numero;
    
    @Column(name = "national_id", nullable = false, length = 20)
    private String nationalId;
    
    @Column(name = "telefono", length = 20)
    private String telefono;
    
    @Column(name = "branch_office", nullable = false, length = 100)
    private String branchOffice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false)
    private QueueType queueType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;
    
    @Column(name = "position_in_queue")
    private Integer positionInQueue;
    
    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_advisor_id")
    private Advisor assignedAdvisor;
    
    @Column(name = "assigned_module_number")
    private Integer assignedModuleNumber;
    
    // Constructors, getters, setters
}
```

#### Advisor Entity

```java
@Entity
@Table(name = "advisor")
public class Advisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdvisorStatus status;
    
    @Column(name = "module_number", nullable = false)
    private Integer moduleNumber;
    
    @Column(name = "assigned_tickets_count")
    private Integer assignedTicketsCount = 0;
    
    @Column(name = "last_assigned_at")
    private LocalDateTime lastAssignedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors, getters, setters
}
```

### 4.2 DTOs (Java 21 Records)

**Aprovechando Java 21 Records para DTOs inmutables:**

```java
// Request DTOs
public record TicketRequest(
    @NotBlank(message = "RUT/ID es obligatorio")
    String nationalId,
    
    @Pattern(regexp = "\\+56\\d{9}", message = "Formato de teléfono inválido")
    String telefono,
    
    @NotBlank(message = "Sucursal es obligatoria")
    String branchOffice,
    
    @NotNull(message = "Tipo de cola es obligatorio")
    QueueType queueType
) {}

// Response DTOs
public record TicketResponse(
    UUID codigoReferencia,
    String numero,
    QueueType queueType,
    TicketStatus status,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    LocalDateTime createdAt
) {}

public record QueuePositionResponse(
    String numero,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    TicketStatus status,
    String assignedAdvisor,
    Integer assignedModuleNumber,
    LocalDateTime lastUpdated
) {}
```

### 4.3 Enumeraciones

```java
public enum QueueType {
    CAJA("Caja", 5, 1, "C"),
    PERSONAL_BANKER("Personal Banker", 15, 2, "P"),
    EMPRESAS("Empresas", 20, 3, "E"),
    GERENCIA("Gerencia", 30, 4, "G");
    
    private final String displayName;
    private final int averageWaitMinutes;
    private final int priority;
    private final String prefix;
    
    // Constructor, getters
}

public enum TicketStatus {
    EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO;
    
    public boolean isActive() {
        return this == EN_ESPERA || this == PROXIMO || this == ATENDIENDO;
    }
}

public enum AdvisorStatus {
    AVAILABLE, BUSY, OFFLINE;
    
    public boolean canReceiveAssignments() {
        return this == AVAILABLE;
    }
}
```

---

## 5. ESTRATEGIA DE PERSISTENCIA CON POSTGRESQL

### 5.1 ¿Por qué PostgreSQL?

**Ventajas Técnicas:**
- **ACID Compliance:** Crítico para transacciones financieras
- **JSONB Support:** Flexibilidad para metadata de auditoría
- **Advanced Indexing:** B-tree, GiST para queries complejas
- **Row-level Locking:** Concurrencia segura para asignaciones
- **Partitioning:** Escalabilidad para tabla de auditoría

**Ventajas Operacionales:**
- **Open Source:** Sin costos de licenciamiento
- **Uptime 99.9%:** Confiabilidad probada
- **Amplia adopción:** Soporte y documentación extensa

### 5.2 Migraciones con Flyway

**¿Por qué Flyway vs Liquibase?**
- **Simplicidad:** Archivos SQL planos vs XML/YAML verboso
- **Integración nativa:** Spring Boot auto-configuración
- **Versionamiento automático:** Control de versiones del esquema
- **Rollback seguro:** Reversión controlada en producción

**Estructura de Migraciones:**

```sql
-- V1__create_ticket_table.sql
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    codigo_referencia UUID UNIQUE NOT NULL,
    numero VARCHAR(10) UNIQUE NOT NULL,
    national_id VARCHAR(20) NOT NULL,
    telefono VARCHAR(20),
    branch_office VARCHAR(100) NOT NULL,
    queue_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_in_queue INTEGER,
    estimated_wait_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_advisor_id BIGINT,
    assigned_module_number INTEGER
);

-- Índices para performance
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
CREATE INDEX idx_ticket_queue_type_status ON ticket(queue_type, status);
CREATE INDEX idx_ticket_created_at ON ticket(created_at);
```

### 5.3 Queries Críticas Optimizadas

**1. Validación de Ticket Activo (RN-001):**
```sql
SELECT COUNT(*) FROM ticket 
WHERE national_id = ? AND status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO');
```

**2. Cálculo de Posición en Cola (RN-010):**
```sql
SELECT COUNT(*) + 1 FROM ticket 
WHERE queue_type = ? AND status IN ('EN_ESPERA', 'PROXIMO') 
AND created_at < ?;
```

**3. Selección de Asesor para Asignación (RN-004):**
```sql
SELECT * FROM advisor 
WHERE status = 'AVAILABLE' 
ORDER BY assigned_tickets_count ASC, last_assigned_at ASC NULLS FIRST 
LIMIT 1;
```

**4. Siguiente Ticket por Prioridad (RN-002, RN-003):**
```sql
WITH prioritized_queues AS (
    SELECT queue_type, 
           CASE queue_type 
               WHEN 'GERENCIA' THEN 4
               WHEN 'EMPRESAS' THEN 3
               WHEN 'PERSONAL_BANKER' THEN 2
               WHEN 'CAJA' THEN 1
           END as priority
    FROM (SELECT DISTINCT queue_type FROM ticket WHERE status IN ('EN_ESPERA', 'PROXIMO')) q
)
SELECT t.* FROM ticket t
JOIN prioritized_queues pq ON t.queue_type = pq.queue_type
WHERE t.status IN ('EN_ESPERA', 'PROXIMO')
ORDER BY pq.priority DESC, t.created_at ASC
LIMIT 1;
```

---

## 6. INTEGRACIÓN CON TELEGRAM

### 6.1 ¿Por qué RestTemplate vs WebClient?

**Decisión: RestTemplate (Síncrono)**

**Justificación:**
- **Volumen bajo:** 0.9 mensajes/segundo no requiere programación reactiva
- **Simplicidad:** API síncrona más fácil de debuggear
- **Stack trace claro:** Debugging más simple
- **Menor curva de aprendizaje:** Equipo familiarizado
- **Suficiente para el throughput:** WebClient sería over-engineering

### 6.2 Implementación TelegramService

```java
@Service
public class TelegramService {
    
    private final RestTemplate restTemplate;
    private final String botToken;
    private final String baseUrl;
    
    public TelegramService(RestTemplate restTemplate, 
                          @Value("${telegram.bot.token}") String botToken) {
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.baseUrl = "https://api.telegram.org/bot" + botToken;
    }
    
    public String enviarMensaje(String chatId, String texto) {
        try {
            var request = Map.of(
                "chat_id", chatId,
                "text", texto,
                "parse_mode", "HTML"
            );
            
            var response = restTemplate.postForObject(
                baseUrl + "/sendMessage", 
                request, 
                TelegramResponse.class
            );
            
            return response.result().messageId();
            
        } catch (Exception e) {
            log.error("Error enviando mensaje a Telegram: {}", e.getMessage());
            throw new TelegramException("Fallo en envío de mensaje", e);
        }
    }
}
```

### 6.3 Plantillas de Mensajes

```java
@Service
public class MessageTemplateService {
    
    public String generarMensaje(MessageTemplate template, TicketContext context) {
        return switch (template) {
            case TOTEM_TICKET_CREADO -> String.format("""
                ✅ <b>Ticket Creado</b>
                
                Tu número de turno: <b>%s</b>
                Posición en cola: <b>#%d</b>
                Tiempo estimado: <b>%d minutos</b>
                
                Te notificaremos cuando estés próximo.
                """, context.numero(), context.posicion(), context.tiempoEstimado());
                
            case TOTEM_PROXIMO_TURNO -> String.format("""
                ⏰ <b>¡Pronto será tu turno!</b>
                
                Turno: <b>%s</b>
                Faltan aproximadamente 3 turnos.
                
                Por favor, acércate a la sucursal.
                """, context.numero());
                
            case TOTEM_ES_TU_TURNO -> String.format("""
                🔔 <b>¡ES TU TURNO %s!</b>
                
                Dirígete al módulo: <b>%d</b>
                Asesor: <b>%s</b>
                """, context.numero(), context.modulo(), context.asesor());
        };
    }
}
```

---

## 7. PROCESAMIENTO ASÍNCRONO

### 7.1 Schedulers con Spring @Scheduled

**MessageScheduler (Cada 60 segundos):**

```java
@Component
@Slf4j
public class MessageScheduler {
    
    private final MensajeRepository mensajeRepository;
    private final TelegramService telegramService;
    
    @Scheduled(fixedRate = 60000) // 60 segundos
    public void procesarMensajesPendientes() {
        log.debug("Iniciando procesamiento de mensajes pendientes");
        
        var mensajesPendientes = mensajeRepository
            .findByEstadoEnvioAndFechaProgramadaLessThanEqual(
                EstadoEnvio.PENDIENTE, 
                LocalDateTime.now()
            );
        
        for (var mensaje : mensajesPendientes) {
            try {
                procesarMensaje(mensaje);
            } catch (Exception e) {
                log.error("Error procesando mensaje {}: {}", mensaje.getId(), e.getMessage());
                manejarErrorEnvio(mensaje, e);
            }
        }
    }
    
    private void procesarMensaje(Mensaje mensaje) {
        var ticket = mensaje.getTicket();
        var texto = messageTemplateService.generarMensaje(
            mensaje.getPlantilla(), 
            TicketContext.from(ticket)
        );
        
        var telegramMessageId = telegramService.enviarMensaje(
            ticket.getTelefono(), 
            texto
        );
        
        mensaje.marcarComoEnviado(telegramMessageId);
        mensajeRepository.save(mensaje);
        
        auditService.registrarEvento(AuditEvent.MENSAJE_ENVIADO, mensaje);
    }
    
    private void manejarErrorEnvio(Mensaje mensaje, Exception error) {
        mensaje.incrementarIntentos();
        
        if (mensaje.getIntentos() >= 3) {
            mensaje.marcarComoFallido();
            auditService.registrarEvento(AuditEvent.MENSAJE_FALLIDO, mensaje);
        } else {
            // Backoff exponencial: 30s, 60s, 120s
            var delayMinutes = (int) Math.pow(2, mensaje.getIntentos() - 1) * 30;
            mensaje.reprogramar(LocalDateTime.now().plusSeconds(delayMinutes));
        }
        
        mensajeRepository.save(mensaje);
    }
}
```

**QueueProcessorScheduler (Cada 5 segundos):**

```java
@Component
@Slf4j
public class QueueProcessorScheduler {
    
    private final QueueManagementService queueManagementService;
    
    @Scheduled(fixedRate = 5000) // 5 segundos
    public void procesarColas() {
        try {
            // 1. Recalcular posiciones
            queueManagementService.recalcularTodasLasPosiciones();
            
            // 2. Actualizar tickets a PROXIMO (posición <= 3)
            queueManagementService.actualizarTicketsProximos();
            
            // 3. Asignar tickets a asesores disponibles
            queueManagementService.procesarAsignacionesAutomaticas();
            
        } catch (Exception e) {
            log.error("Error en procesamiento de colas: {}", e.getMessage(), e);
        }
    }
}
```

### 7.2 Configuración de Scheduling

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("ticketero-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
}
```

---

## 8. BUENAS PRÁCTICAS JAVA 21 + SPRING BOOT

### 8.1 Aprovechamiento de Java 21

**1. Virtual Threads para Schedulers:**

```java
@Configuration
public class VirtualThreadConfig {
    
    @Bean
    public TaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}

// Uso en schedulers
@Async("virtualThreadTaskExecutor")
public CompletableFuture<Void> procesarMensajeAsync(Mensaje mensaje) {
    // Procesamiento asíncrono sin bloquear threads del pool
}
```

**2. Pattern Matching para Validaciones:**

```java
public ValidationResult validarTicketRequest(TicketRequest request) {
    return switch (request.queueType()) {
        case GERENCIA -> validarGerencia(request);
        case EMPRESAS -> validarEmpresas(request);
        case PERSONAL_BANKER -> validarPersonalBanker(request);
        case CAJA -> validarCaja(request);
    };
}
```

**3. Records para DTOs Inmutables:**

```java
// Inmutable, thread-safe, menos boilerplate
public record TicketCreatedEvent(
    UUID ticketId,
    String numero,
    QueueType queueType,
    LocalDateTime timestamp
) implements DomainEvent {}
```

### 8.2 Spring Boot 3.2 Features

**1. Native Compilation Ready:**

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
</plugin>
```

**2. Observability con Micrometer:**

```java
@RestController
@Timed(name = "ticket.creation", description = "Tiempo de creación de tickets")
public class TicketController {
    
    @PostMapping("/api/tickets")
    @Counted(name = "tickets.created", description = "Tickets creados")
    public ResponseEntity<TicketResponse> crearTicket(@Valid @RequestBody TicketRequest request) {
        // Métricas automáticas
    }
}
```

**3. Problem Details (RFC 7807):**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTicketNotFound(TicketNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        problem.setTitle("Ticket no encontrado");
        problem.setProperty("ticketId", ex.getTicketId());
        return ResponseEntity.of(problem).build();
    }
}
```

---

## 9. DEPLOYMENT Y ORQUESTACIÓN

### 9.1 Containerización con Docker

**Multi-stage Dockerfile:**

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/ticketero-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose para Desarrollo:**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ticketero
      POSTGRES_USER: ticketero_user
      POSTGRES_PASSWORD: ticketero_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ticketero
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
    depends_on:
      - postgres

volumes:
  postgres_data:
```

### 9.2 Infraestructura como Código (IaC)

**¿Cuándo usar IaC?**
- **Fase Piloto:** Docker Compose suficiente
- **Fase Expansión:** Terraform + AWS ECS/Fargate
- **Fase Nacional:** Terraform + EKS (Kubernetes)

**Terraform para AWS (Fase Expansión):**

```hcl
# terraform/main.tf
resource "aws_ecs_cluster" "ticketero" {
  name = "ticketero-cluster"
}

resource "aws_ecs_service" "ticketero_api" {
  name            = "ticketero-api"
  cluster         = aws_ecs_cluster.ticketero.id
  task_definition = aws_ecs_task_definition.ticketero_api.arn
  desired_count   = 2
  
  load_balancer {
    target_group_arn = aws_lb_target_group.ticketero_api.arn
    container_name   = "ticketero-api"
    container_port   = 8080
  }
}

resource "aws_rds_instance" "postgres" {
  identifier = "ticketero-postgres"
  engine     = "postgres"
  engine_version = "16"
  instance_class = "db.t3.micro"
  allocated_storage = 20
  
  db_name  = "ticketero"
  username = "ticketero_user"
  password = var.db_password
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
}
```

### 9.3 CI/CD Pipeline

**GitHub Actions Workflow:**

```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run tests
        run: ./mvnw test
      
      - name: Integration tests with Testcontainers
        run: ./mvnw verify -Pintegration-tests
  
  build-and-deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Build Docker image
        run: docker build -t ticketero:${{ github.sha }} .
      
      - name: Deploy to ECS
        run: |
          aws ecs update-service \
            --cluster ticketero-cluster \
            --service ticketero-api \
            --force-new-deployment
```

---

## 10. TESTING STRATEGY

### 10.1 Pirámide de Testing

**1. Unit Tests (70%):**
- Services con mocks de repositories
- Utilities y helpers
- Validaciones de DTOs

**2. Integration Tests (20%):**
- Controllers con @SpringBootTest
- Repositories con @DataJpaTest
- Testcontainers para PostgreSQL

**3. End-to-End Tests (10%):**
- Flujo completo con Testcontainers
- Telegram API mockeado

### 10.2 Testcontainers para Integration Tests

```java
@SpringBootTest
@Testcontainers
class TicketServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ticketero_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void deberiaCrearTicketYCalcularPosicion() {
        // Given
        var request = new TicketRequest("12345678-9", "+56912345678", "Sucursal Centro", QueueType.CAJA);
        
        // When
        var response = ticketService.crearTicket(request);
        
        // Then
        assertThat(response.numero()).startsWith("C");
        assertThat(response.positionInQueue()).isEqualTo(1);
        assertThat(response.estimatedWaitMinutes()).isEqualTo(5);
    }
}
```

---

## 11. MONITOREO Y OBSERVABILIDAD

### 11.1 Métricas con Micrometer

**Métricas de Negocio:**
- Tickets creados por minuto
- Tiempo promedio de atención por cola
- Tasa de éxito de mensajes Telegram
- Asesores disponibles vs ocupados

**Métricas Técnicas:**
- Response time de endpoints
- Throughput de requests
- Errores HTTP por endpoint
- Uso de memoria y CPU

### 11.2 Health Checks

```java
@Component
public class TelegramHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            telegramService.getMe(); // Telegram API health check
            return Health.up()
                .withDetail("telegram", "Bot activo")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("telegram", "Bot inaccesible")
                .withException(e)
                .build();
        }
    }
}
```

### 11.3 Logging Estructurado

```java
@Slf4j
@Service
public class TicketService {
    
    public TicketResponse crearTicket(TicketRequest request) {
        log.info("Creando ticket para cliente: {}, cola: {}", 
                request.nationalId(), request.queueType());
        
        try {
            var ticket = // ... lógica de creación
            
            log.info("Ticket creado exitosamente: {}, posición: {}", 
                    ticket.getNumero(), ticket.getPositionInQueue());
            
            return TicketResponse.from(ticket);
            
        } catch (Exception e) {
            log.error("Error creando ticket para cliente: {}", 
                    request.nationalId(), e);
            throw e;
        }
    }
}
```

---

## 12. CONSIDERACIONES DE SEGURIDAD

### 12.1 Protección de Datos Sensibles

**Encriptación de Campos:**
```java
@Entity
public class Ticket {
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "national_id")
    private String nationalId;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "telefono")
    private String telefono;
}
```

**Configuración de Encriptación:**
```yaml
app:
  encryption:
    key: ${ENCRYPTION_KEY:default-key-for-dev}
    algorithm: AES/GCB/PKCS5Padding
```

### 12.2 Validación y Sanitización

```java
@RestController
@Validated
public class TicketController {
    
    @PostMapping("/api/tickets")
    public ResponseEntity<TicketResponse> crearTicket(
            @Valid @RequestBody TicketRequest request,
            HttpServletRequest httpRequest) {
        
        // Rate limiting por IP
        rateLimitService.checkRateLimit(httpRequest.getRemoteAddr());
        
        // Sanitización de inputs
        var sanitizedRequest = sanitizationService.sanitize(request);
        
        return ResponseEntity.ok(ticketService.crearTicket(sanitizedRequest));
    }
}
```

---

## 13. ROADMAP DE IMPLEMENTACIÓN

### 13.1 Fase 1: MVP (4 semanas)

**Semana 1-2: Core Backend**
- Setup proyecto Spring Boot 3.2
- Entidades JPA + Flyway migrations
- TicketService básico (crear, consultar)
- Tests unitarios

**Semana 3: Integración Telegram**
- TelegramService + RestTemplate
- MessageScheduler básico
- Plantillas de mensajes
- Tests de integración

**Semana 4: Dashboard Admin**
- AdminController + endpoints
- QueueManagementService
- Docker Compose para desarrollo
- Deployment en staging

### 13.2 Fase 2: Optimización (2 semanas)

**Semana 5: Performance**
- Optimización de queries
- Índices de base de datos
- Métricas con Micrometer
- Load testing

**Semana 6: Producción**
- Terraform para AWS
- CI/CD pipeline
- Monitoreo y alertas
- Deployment en producción

### 13.3 Fase 3: Escalabilidad (2 semanas)

**Semana 7-8: Mejoras**
- Virtual Threads para schedulers
- Caching con Redis (si es necesario)
- Optimizaciones adicionales
- Documentación técnica

---

## 14. DECISIONES TÉCNICAS CLAVE

### 14.1 ¿Monolito vs Microservicios?
**Decisión: Monolito Modular**
- Volumen bajo (0.3 ops/seg)
- Dominio cohesivo
- Equipo pequeño
- Time-to-market crítico

### 14.2 ¿RestTemplate vs WebClient?
**Decisión: RestTemplate**
- Throughput bajo (0.9 msg/seg)
- Simplicidad de debugging
- Stack trace claro
- Menor curva de aprendizaje

### 14.3 ¿Flyway vs Liquibase?
**Decisión: Flyway**
- SQL plano vs XML verboso
- Integración nativa Spring Boot
- Simplicidad para el equipo

### 14.4 ¿Scheduling vs Message Queue?
**Decisión: @Scheduled**
- Volumen bajo no justifica RabbitMQ/Kafka
- Menos infraestructura
- Suficiente para el throughput

### 14.5 ¿Docker Compose vs Kubernetes?
**Decisión: Docker Compose → ECS → EKS**
- Fase Piloto: Docker Compose
- Fase Expansión: AWS ECS/Fargate
- Fase Nacional: EKS si es necesario

---

## 15. RIESGOS Y MITIGACIONES

### 15.1 Riesgos Técnicos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Telegram API down | Media | Alto | Retry logic + fallback SMS |
| PostgreSQL performance | Baja | Alto | Índices optimizados + monitoring |
| Concurrencia en asignaciones | Media | Medio | Row-level locking + tests |
| Memory leaks en schedulers | Baja | Medio | Virtual Threads + monitoring |

### 15.2 Riesgos de Negocio

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Cambio de requerimientos | Alta | Medio | Arquitectura modular flexible |
| Escalabilidad inesperada | Media | Alto | Monitoreo + plan de escalamiento |
| Integración con sistemas legacy | Media | Alto | APIs REST bien definidas |

---

## 16. CONCLUSIONES

### 16.1 Fortalezas de la Arquitectura Propuesta

1. **Simplicidad:** Monolito modular fácil de entender y mantener
2. **Tecnologías maduras:** Java 21 + Spring Boot 3.2 + PostgreSQL
3. **Escalabilidad gradual:** Docker Compose → ECS → EKS
4. **Observabilidad:** Métricas, logs y health checks integrados
5. **Testing:** Estrategia completa con Testcontainers

### 16.2 Preparación para el Futuro

- **Modularidad:** Fácil extracción a microservicios si es necesario
- **Cloud-native:** Preparado para contenedores y orquestación
- **Observabilidad:** Métricas y trazabilidad desde el inicio
- **Seguridad:** Encriptación y validación incorporadas

### 16.3 Recomendación Final

La arquitectura propuesta es **pragmática y apropiada** para el volumen y complejidad del proyecto. Evita sobre-ingeniería mientras mantiene flexibilidad para crecimiento futuro.

**Próximo paso:** Iniciar implementación con Fase 1 (MVP en 4 semanas).

---

**Preparado por:** Arquitecto de Software Senior  
**Fecha:** Diciembre 2025  
**Versión:** 1.0
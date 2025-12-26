# 📋 Lineamientos de Desarrollo - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Contexto del Sistema](#contexto-del-sistema)
2. [Arquitectura y Patrones](#arquitectura-y-patrones)
3. [Estructura de Paquetes](#estructura-de-paquetes)
4. [Convenciones de Código](#convenciones-de-código)
5. [Patrones de Testing](#patrones-de-testing)
6. [Guías para Nuevas Funcionalidades](#guías-para-nuevas-funcionalidades)
7. [Mejores Prácticas](#mejores-prácticas)
8. [Plantillas de Código](#plantillas-de-código)

---

## 🎯 Contexto del Sistema

### Descripción General
Sistema de gestión de tickets para sucursales bancarias con notificaciones en tiempo real vía Telegram. Implementa patrones de arquitectura empresarial con Spring Boot 3.2 y Java 21.

### Stack Tecnológico
- **Backend**: Spring Boot 3.2, Java 21
- **Base de Datos**: PostgreSQL 16 con Flyway
- **Mensajería**: RabbitMQ 3.13 + Patrón Outbox
- **Notificaciones**: Telegram Bot API
- **Testing**: JUnit 5, TestContainers, RestAssured, Mockito
- **Build**: Maven 3.9+

### Características Clave
- 4 tipos de colas de atención (CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA)
- 3 tipos de notificaciones automáticas
- Patrón Outbox para mensajería confiable
- Recuperación automática de fallos
- Cobertura de código con JaCoCo

---

## 🏗️ Arquitectura y Patrones

### Arquitectura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Controllers   │  │   CLI Interface │  │  Schedulers │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │    Services     │  │      DTOs       │  │   Mappers   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │    Entities     │  │      Enums      │  │  Validators │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  Repositories   │  │   External APIs │  │   Config    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Patrones Implementados

#### 1. **Repository Pattern**
```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);
}
```

#### 2. **Service Layer Pattern**
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {
    private final TicketRepository ticketRepository;
    // Lógica de negocio aquí
}
```

#### 3. **Outbox Pattern**
```java
@Entity
@Table(name = "outbox_message")
public class OutboxMessage {
    // Garantiza consistencia eventual en mensajería
}
```

#### 4. **Builder Pattern**
```java
Ticket ticket = Ticket.builder()
    .nationalId(request.nationalId())
    .queueType(queueType)
    .status(TicketStatus.EN_ESPERA)
    .build();
```

#### 5. **Strategy Pattern** (Enums con comportamiento)
```java
public enum QueueType {
    CAJA("Caja", 5, 1),
    PERSONAL_BANKER("Personal Banker", 15, 2);
    
    public char getPrefix() {
        return switch (this) {
            case CAJA -> 'C';
            case PERSONAL_BANKER -> 'P';
        };
    }
}
```

---

## 📁 Estructura de Paquetes

### Organización Estándar
```
com.example.ticketero/
├── cli/                    # Interfaz de línea de comandos
├── config/                 # Configuraciones de Spring
├── controller/             # Controladores REST
├── model/
│   ├── dto/               # Data Transfer Objects
│   ├── entity/            # Entidades JPA
│   └── enums/             # Enumeraciones del dominio
├── repository/            # Repositorios JPA
├── scheduler/             # Tareas programadas
├── service/               # Lógica de negocio
└── TicketeroApplication.java
```

### Principios de Organización

#### 1. **Separación por Capas**
- **Controllers**: Solo manejo de HTTP, validación básica
- **Services**: Lógica de negocio, transacciones
- **Repositories**: Acceso a datos
- **DTOs**: Contratos de API

#### 2. **Cohesión Funcional**
- Cada paquete agrupa funcionalidad relacionada
- Dependencias unidireccionales (Controller → Service → Repository)

#### 3. **Nomenclatura Consistente**
- Sufijos descriptivos: `Controller`, `Service`, `Repository`
- Nombres en inglés para código, español para datos de negocio

---

## 📝 Convenciones de Código

### Nomenclatura

#### Clases y Interfaces
```java
// ✅ Correcto
public class TicketService { }
public interface TicketRepository { }
public enum QueueType { }

// ❌ Incorrecto
public class ticketService { }
public class TicketSvc { }
```

#### Métodos
```java
// ✅ Correcto - Verbos descriptivos
public TicketResponse crearTicket(TicketCreateRequest request)
public void finalizarTicket(Long ticketId)
public Optional<Ticket> obtenerTicketPorNumero(String numero)

// ❌ Incorrecto
public TicketResponse create(TicketCreateRequest request)
public void finish(Long id)
```

#### Variables y Campos
```java
// ✅ Correcto
private final TicketRepository ticketRepository;
private final TelegramService telegramService;
Long estimatedWaitMinutes;

// ❌ Incorrecto
private final TicketRepository repo;
private final TelegramService tgService;
Long waitTime;
```

### Anotaciones Estándar

#### Servicios
```java
@Service
@RequiredArgsConstructor  // Constructor con final fields
@Slf4j               // Logging
@Transactional       // Transacciones por defecto
public class TicketService {
    private final TicketRepository ticketRepository;
}
```

#### Controladores
```java
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class TicketController {
    private final TicketService ticketService;
}
```

#### Entidades
```java
@Entity
@Table(name = "ticket")
@Data                    // Getters/Setters
@NoArgsConstructor      // Constructor vacío para JPA
@AllArgsConstructor     // Constructor completo
@Builder                // Patrón Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

### Manejo de Errores

#### Validación de Entrada
```java
public TicketResponse crearTicket(TicketCreateRequest request) {
    if (request == null) {
        throw new IllegalArgumentException("Request no puede ser null");
    }
    
    if (request.nationalId() == null || request.nationalId().trim().isEmpty()) {
        throw new IllegalArgumentException("National ID es obligatorio");
    }
    
    // Lógica del método...
}
```

#### Manejo de Estados
```java
public void llamarTicket(Long ticketId, Long advisorId) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado: " + ticketId));
    
    if (ticket.getStatus() != TicketStatus.EN_ESPERA) {
        throw new IllegalStateException("Ticket no está en espera: " + ticket.getStatus());
    }
    
    // Lógica del método...
}
```

### Logging

#### Niveles de Log
```java
@Slf4j
public class TicketService {
    
    public TicketResponse crearTicket(TicketCreateRequest request) {
        log.info("Creando ticket para RUT: {}", request.nationalId());
        
        try {
            // Lógica...
            log.debug("Ticket creado exitosamente: {}", ticket.getNumero());
            return response;
        } catch (Exception e) {
            log.error("Error creando ticket para RUT {}: {}", request.nationalId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

---

## 🧪 Patrones de Testing

### Estructura de Tests

```
src/test/java/com/example/ticketero/
├── controller/           # Tests de controladores (MockMvc)
├── service/             # Tests unitarios de servicios
├── integration/         # Tests de integración (TestContainers)
├── scheduler/           # Tests de tareas programadas
└── testutil/           # Utilidades y builders de test
```

### Tests Unitarios

#### Patrón AAA (Arrange-Act-Assert)
```java
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    
    @Mock
    private TicketRepository ticketRepository;
    
    @InjectMocks
    private TicketService ticketService;
    
    @Test
    @DisplayName("Debe crear ticket exitosamente con datos válidos")
    void debeCrearTicketExitosamente() {
        // Arrange
        TicketCreateRequest request = TestDataBuilder.validTicketRequest();
        when(ticketRepository.countByQueueTypeAndStatus(any(), any())).thenReturn(0L);
        when(ticketRepository.save(any())).thenReturn(TestDataBuilder.ticketWaiting().build());
        
        // Act
        TicketResponse response = ticketService.crearTicket(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.numero()).startsWith("C");
        verify(ticketRepository).save(any(Ticket.class));
    }
}
```

### Tests de Integración

#### Base Class Pattern
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");
    
    @BeforeEach
    void cleanDatabase() {
        // Limpiar datos entre tests
    }
}
```

#### Test de API Completo
```java
class TicketCreationIT extends BaseIntegrationTest {
    
    @Test
    @DisplayName("Debe crear ticket y enviar notificación")
    void debeCrearTicketYEnviarNotificacion() {
        // Given
        String requestBody = createTicketRequest("12345678", "CAJA");
        
        // When
        ValidatableResponse response = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/tickets")
        .then()
            .statusCode(201);
        
        // Then
        response.body("numero", startsWith("C"))
                .body("status", equalTo("EN_ESPERA"))
                .body("positionInQueue", equalTo(1));
        
        // Verificar en base de datos
        assertThat(countTicketsInStatus("EN_ESPERA")).isEqualTo(1);
    }
}
```

### Test Data Builder Pattern

```java
public class TestDataBuilder {
    
    public static Ticket.TicketBuilder ticketWaiting() {
        return Ticket.builder()
            .id(1L)
            .numero("C001")
            .nationalId("12345678")
            .queueType(QueueType.CAJA)
            .status(TicketStatus.EN_ESPERA)
            .positionInQueue(1)
            .estimatedWaitMinutes(5);
    }
    
    public static TicketCreateRequest validTicketRequest() {
        return new TicketCreateRequest(
            "12345678",
            "+56912345678",
            "Sucursal Centro",
            QueueType.CAJA
        );
    }
}
```

---

## 🔧 Guías para Nuevas Funcionalidades

### Agregar Nueva Cola de Atención

#### 1. Actualizar Enum
```java
public enum QueueType {
    CAJA("Caja", 5, 1),
    PERSONAL_BANKER("Personal Banker", 15, 2),
    EMPRESAS("Empresas", 20, 3),
    GERENCIA("Gerencia", 30, 4),
    NUEVA_COLA("Nueva Cola", 25, 5);  // ← Agregar aquí
    
    public char getPrefix() {
        return switch (this) {
            case CAJA -> 'C';
            case PERSONAL_BANKER -> 'P';
            case EMPRESAS -> 'E';
            case GERENCIA -> 'G';
            case NUEVA_COLA -> 'N';  // ← Agregar prefijo
        };
    }
}
```

#### 2. Actualizar Tests
```java
@ParameterizedTest
@EnumSource(QueueType.class)
@DisplayName("Debe crear ticket para todos los tipos de cola")
void debeCrearTicketParaTodosLosTipos(QueueType queueType) {
    // Test parametrizado para todas las colas
}
```

### Agregar Nuevo Tipo de Notificación

#### 1. Actualizar Enum de Plantillas
```java
public enum MessageTemplate {
    TOTEM_TICKET_CREADO("totem_ticket_creado"),
    TOTEM_PROXIMO_TURNO("totem_proximo_turno"),
    TOTEM_ES_TU_TURNO("totem_es_tu_turno"),
    NUEVA_NOTIFICACION("nueva_notificacion");  // ← Agregar aquí
}
```

#### 2. Implementar en TelegramService
```java
public String obtenerTextoMensaje(String plantilla, String numero, 
                                 Integer posicion, Integer tiempoEstimado,
                                 String asesor, Integer modulo) {
    return switch (plantilla) {
        case "totem_ticket_creado" -> formatearTicketCreado(numero, posicion, tiempoEstimado);
        case "totem_proximo_turno" -> formatearProximoTurno(numero, posicion, tiempoEstimado);
        case "totem_es_tu_turno" -> formatearEsTuTurno(numero, asesor, modulo);
        case "nueva_notificacion" -> formatearNuevaNotificacion(numero);  // ← Implementar
        default -> throw new IllegalArgumentException("Plantilla no soportada: " + plantilla);
    };
}
```

### Agregar Nuevo Endpoint

#### 1. Crear DTO de Request/Response
```java
public record NuevoRequest(
    @NotBlank(message = "Campo obligatorio")
    String campo1,
    
    @Valid
    @NotNull(message = "Campo obligatorio")
    String campo2
) {}

public record NuevoResponse(
    Long id,
    String campo1,
    String campo2,
    LocalDateTime createdAt
) {}
```

#### 2. Implementar en Service
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NuevoService {
    
    public NuevoResponse procesarNuevo(NuevoRequest request) {
        // Validaciones
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }
        
        // Lógica de negocio
        log.info("Procesando nuevo request: {}", request.campo1());
        
        // Retornar response
        return new NuevoResponse(1L, request.campo1(), request.campo2(), LocalDateTime.now());
    }
}
```

#### 3. Crear Controller
```java
@RestController
@RequestMapping("/api/nuevo")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class NuevoController {
    
    private final NuevoService nuevoService;
    
    @PostMapping
    public ResponseEntity<NuevoResponse> procesarNuevo(@Valid @RequestBody NuevoRequest request) {
        try {
            NuevoResponse response = nuevoService.procesarNuevo(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

#### 4. Crear Tests
```java
@ExtendWith(MockitoExtension.class)
class NuevoServiceTest {
    
    @Mock
    private NuevoRepository nuevoRepository;
    
    @InjectMocks
    private NuevoService nuevoService;
    
    @Test
    @DisplayName("Debe procesar request válido exitosamente")
    void debeProcesarRequestValido() {
        // Arrange
        NuevoRequest request = new NuevoRequest("valor1", "valor2");
        
        // Act
        NuevoResponse response = nuevoService.procesarNuevo(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.campo1()).isEqualTo("valor1");
    }
}
```

---

## ✨ Mejores Prácticas

### Transacciones

#### Uso de @Transactional
```java
@Service
@Transactional  // Por defecto para toda la clase
public class TicketService {
    
    @Transactional(readOnly = true)  // Solo lectura para consultas
    public List<TicketResponse> obtenerTicketsActivos() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.EN_ESPERA)
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }
    
    @Transactional(rollbackFor = Exception.class)  // Rollback explícito
    public void operacionCritica() {
        // Operación que requiere rollback en cualquier excepción
    }
}
```

### Validaciones

#### Bean Validation
```java
public record TicketCreateRequest(
    @NotBlank(message = "El RUT/ID es obligatorio")
    String nationalId,
    
    @Pattern(regexp = "^\\+56[0-9]{9}$", message = "Teléfono debe tener formato +56XXXXXXXXX")
    String telefono,
    
    @NotBlank(message = "La sucursal es obligatoria")
    String branchOffice,
    
    @NotNull(message = "El tipo de cola es obligatorio")
    QueueType queueType
) {}
```

#### Validaciones de Negocio
```java
private void validarTicketUnico(String nationalId) {
    List<TicketStatus> estadosActivos = TicketStatus.getActiveStatuses();
    Optional<Ticket> ticketExistente = ticketRepository
        .findByNationalIdAndStatusIn(nationalId, estadosActivos);
    
    if (ticketExistente.isPresent()) {
        throw new IllegalStateException(
            "Ya existe un ticket activo para este RUT/ID: " + 
            ticketExistente.get().getNumero()
        );
    }
}
```

### Performance

#### Consultas Optimizadas
```java
// ✅ Correcto - Query específica
@Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt ASC")
List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);

// ❌ Incorrecto - Cargar todo y filtrar en memoria
List<Ticket> findAll().stream()
    .filter(t -> t.getStatus() == status)
    .sorted(Comparator.comparing(Ticket::getCreatedAt))
    .toList();
```

#### Lazy Loading
```java
@Entity
public class Ticket {
    @ManyToOne(fetch = FetchType.LAZY)  // ✅ Lazy por defecto
    @JoinColumn(name = "assigned_advisor_id")
    private Advisor assignedAdvisor;
}
```

### Seguridad

#### Sanitización de Entrada
```java
public TicketResponse crearTicket(TicketCreateRequest request) {
    String nationalId = request.nationalId().trim().toUpperCase();
    String telefono = request.telefono() != null ? request.telefono().trim() : null;
    String branchOffice = request.branchOffice().trim();
    
    // Usar valores sanitizados...
}
```

#### Logging Seguro
```java
// ✅ Correcto - No exponer datos sensibles
log.info("Creando ticket para RUT: {}", maskRut(request.nationalId()));

// ❌ Incorrecto - Exponer datos completos
log.info("Request completo: {}", request);
```

---

## 📋 Plantillas de Código

### Nueva Entidad JPA

```java
package com.example.ticketero.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nueva_entidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NuevaEntidad {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campo_obligatorio", nullable = false, length = 100)
    private String campoObligatorio;

    @Column(name = "campo_opcional")
    private String campoOpcional;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Nuevo Repository

```java
package com.example.ticketero.repository;

import com.example.ticketero.model.entity.NuevaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NuevaEntidadRepository extends JpaRepository<NuevaEntidad, Long> {

    Optional<NuevaEntidad> findByCampoObligatorio(String campoObligatorio);

    @Query("SELECT n FROM NuevaEntidad n WHERE n.campoOpcional = :valor ORDER BY n.createdAt DESC")
    List<NuevaEntidad> findByCampoOpcionalOrderByCreatedAtDesc(@Param("valor") String valor);

    @Query("SELECT COUNT(n) FROM NuevaEntidad n WHERE n.campoObligatorio = :campo")
    Long countByCampoObligatorio(@Param("campo") String campo);
}
```

### Nuevo Service

```java
package com.example.ticketero.service;

import com.example.ticketero.model.dto.NuevaEntidadRequest;
import com.example.ticketero.model.dto.NuevaEntidadResponse;
import com.example.ticketero.model.entity.NuevaEntidad;
import com.example.ticketero.repository.NuevaEntidadRepository;
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
public class NuevaEntidadService {

    private final NuevaEntidadRepository nuevaEntidadRepository;

    public NuevaEntidadResponse crear(NuevaEntidadRequest request) {
        // Validaciones
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }

        log.info("Creando nueva entidad: {}", request.campoObligatorio());

        // Crear entidad
        NuevaEntidad entidad = NuevaEntidad.builder()
            .campoObligatorio(request.campoObligatorio().trim())
            .campoOpcional(request.campoOpcional() != null ? request.campoOpcional().trim() : null)
            .build();

        entidad = nuevaEntidadRepository.save(entidad);

        log.info("Nueva entidad creada con ID: {}", entidad.getId());

        return convertirAResponse(entidad);
    }

    @Transactional(readOnly = true)
    public List<NuevaEntidadResponse> obtenerTodas() {
        return nuevaEntidadRepository.findAll()
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<NuevaEntidadResponse> obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID debe ser positivo");
        }

        return nuevaEntidadRepository.findById(id)
            .map(this::convertirAResponse);
    }

    private NuevaEntidadResponse convertirAResponse(NuevaEntidad entidad) {
        return new NuevaEntidadResponse(
            entidad.getId(),
            entidad.getCampoObligatorio(),
            entidad.getCampoOpcional(),
            entidad.getCreatedAt(),
            entidad.getUpdatedAt()
        );
    }
}
```

### Test de Integración

```java
package com.example.ticketero.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class NuevaEntidadIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Debe crear nueva entidad exitosamente")
    void debeCrearNuevaEntidadExitosamente() {
        // Given
        String requestBody = """
            {
                "campoObligatorio": "valor test",
                "campoOpcional": "valor opcional"
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/nueva-entidad")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("campoObligatorio", equalTo("valor test"))
            .body("campoOpcional", equalTo("valor opcional"))
            .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("Debe rechazar request con campo obligatorio vacío")
    void debeRechazarRequestConCampoVacio() {
        // Given
        String requestBody = """
            {
                "campoObligatorio": "",
                "campoOpcional": "valor opcional"
            }
            """;

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/nueva-entidad")
        .then()
            .statusCode(400);
    }
}
```

---

## 🎯 Checklist para Nuevas Funcionalidades

### Antes de Implementar
- [ ] Definir claramente los requerimientos
- [ ] Diseñar la API (request/response)
- [ ] Identificar entidades y relaciones necesarias
- [ ] Planificar migraciones de base de datos
- [ ] Considerar impacto en funcionalidades existentes

### Durante la Implementación
- [ ] Seguir patrones de arquitectura establecidos
- [ ] Implementar validaciones adecuadas
- [ ] Agregar logging apropiado
- [ ] Manejar errores correctamente
- [ ] Escribir tests unitarios y de integración

### Después de Implementar
- [ ] Ejecutar suite completa de tests
- [ ] Verificar cobertura de código
- [ ] Actualizar documentación
- [ ] Realizar pruebas manuales
- [ ] Considerar impacto en performance

---

**📞 Soporte**: Para dudas sobre patrones o implementación, consultar este documento o revisar ejemplos existentes en el código base.
# **PROMPT 3: PLAN DETALLADO \- Checklist de Implementación del Sistema Ticketero**

## **Contexto**

Eres un Tech Lead Senior responsable de crear el plan de implementación detallado del Sistema Ticketero. Has recibido el documento de Arquitectura aprobado y tu tarea es generar un **plan de implementación paso a paso** que cualquier desarrollador pueda seguir para construir el sistema completo.

---

## **Documentos de Entrada**

**Lee estos archivos que YA están en tu proyecto:**

1. `docs/REQUERIMIENTOS-NEGOCIO.md` \- Contexto de negocio  
2. `REQUERIMIENTOS-FUNCIONALES.md` \- RF-001 a RF-008  
3. `docs/ARQUITECTURA.md` \- Stack tecnológico, diagramas, componentes

---

## **Tu Tarea**

Crear un documento de **Plan Detallado de Implementación** que incluya:

1. **Estructura de Paquetes Java** completa  
2. **Migraciones SQL** (3 archivos Flyway)  
3. **Configuración del Proyecto** (pom.xml, application.yml, .env)  
4. **Checklist de Implementación** por fases (Fase 0 a Fase 7\)  
5. **Orden de Implementación** (Entities → Repositories → Services → Controllers → Schedulers)  
6. **Criterios de Aceptación** por fase

---

## **PARTE 1: Estructura del Proyecto**

Define la estructura completa de carpetas y paquetes:

ticketero/  
├── pom.xml                                    \# Maven configuration  
├── .env                                       \# Variables de entorno (gitignored)  
├── docker-compose.yml                         \# PostgreSQL \+ API  
├── Dockerfile                                 \# Multi-stage build  
├── README.md                                  \# Instrucciones del proyecto  
│  
├── src/  
│   ├── main/  
│   │   ├── java/com/example/ticketero/  
│   │   │   │  
│   │   │   ├── TicketeroApplication.java    \# Main class con @EnableScheduling  
│   │   │   │  
│   │   │   ├── controller/                   \# REST Controllers  
│   │   │   │   ├── TicketController.java  
│   │   │   │   └── AdminController.java  
│   │   │   │  
│   │   │   ├── service/                      \# Business Logic  
│   │   │   │   ├── TicketService.java  
│   │   │   │   ├── TelegramService.java  
│   │   │   │   ├── QueueManagementService.java  
│   │   │   │   ├── AdvisorService.java  
│   │   │   │   └── NotificationService.java  
│   │   │   │  
│   │   │   ├── repository/                   \# Data Access  
│   │   │   │   ├── TicketRepository.java  
│   │   │   │   ├── MensajeRepository.java  
│   │   │   │   └── AdvisorRepository.java  
│   │   │   │  
│   │   │   ├── model/  
│   │   │   │   ├── entity/                   \# JPA Entities  
│   │   │   │   │   ├── Ticket.java  
│   │   │   │   │   ├── Mensaje.java  
│   │   │   │   │   └── Advisor.java  
│   │   │   │   │  
│   │   │   │   ├── dto/                      \# DTOs  
│   │   │   │   │   ├── TicketCreateRequest.java  
│   │   │   │   │   ├── TicketResponse.java  
│   │   │   │   │   ├── QueuePositionResponse.java  
│   │   │   │   │   ├── DashboardResponse.java  
│   │   │   │   │   └── QueueStatusResponse.java  
│   │   │   │   │  
│   │   │   │   └── enums/                    \# Enumerations  
│   │   │   │       ├── QueueType.java  
│   │   │   │       ├── TicketStatus.java  
│   │   │   │       ├── AdvisorStatus.java  
│   │   │   │       └── MessageTemplate.java  
│   │   │   │  
│   │   │   ├── scheduler/                    \# Scheduled Tasks  
│   │   │   │   ├── MensajeScheduler.java  
│   │   │   │   └── QueueProcessorScheduler.java  
│   │   │   │  
│   │   │   ├── config/                       \# Configuration  
│   │   │   │   ├── RestTemplateConfig.java  
│   │   │   │   └── TelegramConfig.java  
│   │   │   │  
│   │   │   └── exception/                    \# Exception Handling  
│   │   │       ├── TicketNotFoundException.java  
│   │   │       ├── TicketActivoExistenteException.java  
│   │   │       └── GlobalExceptionHandler.java  
│   │   │  
│   │   └── resources/  
│   │       ├── application.yml               \# Spring Boot config  
│   │       ├── application-dev.yml           \# Dev profile  
│   │       ├── application-prod.yml          \# Prod profile  
│   │       │  
│   │       └── db/migration/                 \# Flyway migrations  
│   │           ├── V1\_\_create\_ticket\_table.sql  
│   │           ├── V2\_\_create\_mensaje\_table.sql  
│   │           └── V3\_\_create\_advisor\_table.sql  
│   │  
│   └── test/  
│       └── java/com/example/ticketero/  
│           ├── service/  
│           │   ├── TicketServiceTest.java  
│           │   └── TelegramServiceTest.java  
│           │  
│           └── controller/  
│               └── TicketControllerTest.java  
│  
└── docs/                                      \# Documentación  
    ├── REQUERIMIENTOS-NEGOCIO.md  
    ├── ARQUITECTURA.md  
    └── diagrams/  
        ├── 01-context-diagram.puml  
        ├── 02-sequence-diagram.puml  
        └── 03-er-diagram.puml

---

## **PARTE 2: Migraciones SQL (Flyway)**

### **Migración V1: Tabla Ticket**

**Archivo:** `src/main/resources/db/migration/V1__create_ticket_table.sql`

\-- V1\_\_create\_ticket\_table.sql  
\-- Tabla principal de tickets

CREATE TABLE ticket (  
    id BIGSERIAL PRIMARY KEY,  
    codigo\_referencia UUID NOT NULL UNIQUE,  
    numero VARCHAR(10) NOT NULL UNIQUE,  
    national\_id VARCHAR(20) NOT NULL,  
    telefono VARCHAR(20),  
    branch\_office VARCHAR(100) NOT NULL,  
    queue\_type VARCHAR(20) NOT NULL,  
    status VARCHAR(20) NOT NULL,  
    position\_in\_queue INTEGER NOT NULL,  
    estimated\_wait\_minutes INTEGER NOT NULL,  
    assigned\_advisor\_id BIGINT,  
    assigned\_module\_number INTEGER,  
    created\_at TIMESTAMP NOT NULL DEFAULT CURRENT\_TIMESTAMP,  
    updated\_at TIMESTAMP NOT NULL DEFAULT CURRENT\_TIMESTAMP  
);

\-- Índices para performance  
CREATE INDEX idx\_ticket\_status ON ticket(status);  
CREATE INDEX idx\_ticket\_national\_id ON ticket(national\_id);  
CREATE INDEX idx\_ticket\_queue\_type ON ticket(queue\_type);  
CREATE INDEX idx\_ticket\_created\_at ON ticket(created\_at DESC);

\-- Comentarios para documentación  
COMMENT ON TABLE ticket IS 'Tickets de atención en sucursales';  
COMMENT ON COLUMN ticket.codigo\_referencia IS 'UUID único para referencias externas';  
COMMENT ON COLUMN ticket.numero IS 'Número visible del ticket (C01, P15, etc.)';  
COMMENT ON COLUMN ticket.position\_in\_queue IS 'Posición actual en cola (calculada en tiempo real)';  
COMMENT ON COLUMN ticket.estimated\_wait\_minutes IS 'Tiempo estimado de espera en minutos';

---

### **Migración V2: Tabla Mensaje**

**Archivo:** `src/main/resources/db/migration/V2__create_mensaje_table.sql`

\-- V2\_\_create\_mensaje\_table.sql  
\-- Tabla de mensajes programados para Telegram

CREATE TABLE mensaje (  
    id BIGSERIAL PRIMARY KEY,  
    ticket\_id BIGINT NOT NULL,  
    plantilla VARCHAR(50) NOT NULL,  
    estado\_envio VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',  
    fecha\_programada TIMESTAMP NOT NULL,  
    fecha\_envio TIMESTAMP,  
    telegram\_message\_id VARCHAR(50),  
    intentos INTEGER NOT NULL DEFAULT 0,  
    created\_at TIMESTAMP NOT NULL DEFAULT CURRENT\_TIMESTAMP,  
      
    CONSTRAINT fk\_mensaje\_ticket   
        FOREIGN KEY (ticket\_id)   
        REFERENCES ticket(id)   
        ON DELETE CASCADE  
);

\-- Índices para performance del scheduler  
CREATE INDEX idx\_mensaje\_estado\_fecha ON mensaje(estado\_envio, fecha\_programada);  
CREATE INDEX idx\_mensaje\_ticket\_id ON mensaje(ticket\_id);

\-- Comentarios  
COMMENT ON TABLE mensaje IS 'Mensajes programados para envío vía Telegram';  
COMMENT ON COLUMN mensaje.plantilla IS 'Tipo de mensaje: totem\_ticket\_creado, totem\_proximo\_turno, totem\_es\_tu\_turno';  
COMMENT ON COLUMN mensaje.estado\_envio IS 'Estado: PENDIENTE, ENVIADO, FALLIDO';  
COMMENT ON COLUMN mensaje.intentos IS 'Cantidad de reintentos de envío';

---

### **Migración V3: Tabla Advisor**

**Archivo:** `src/main/resources/db/migration/V3__create_advisor_table.sql`

\-- V3\_\_create\_advisor\_table.sql  
\-- Tabla de asesores/ejecutivos

CREATE TABLE advisor (  
    id BIGSERIAL PRIMARY KEY,  
    name VARCHAR(100) NOT NULL,  
    email VARCHAR(100) NOT NULL UNIQUE,  
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',  
    module\_number INTEGER NOT NULL,  
    assigned\_tickets\_count INTEGER NOT NULL DEFAULT 0,  
    created\_at TIMESTAMP NOT NULL DEFAULT CURRENT\_TIMESTAMP,  
    updated\_at TIMESTAMP NOT NULL DEFAULT CURRENT\_TIMESTAMP,  
      
    CONSTRAINT chk\_module\_number CHECK (module\_number BETWEEN 1 AND 5),  
    CONSTRAINT chk\_assigned\_count CHECK (assigned\_tickets\_count \>= 0\)  
);

\-- Índice para búsqueda de asesores disponibles  
CREATE INDEX idx\_advisor\_status ON advisor(status);  
CREATE INDEX idx\_advisor\_module ON advisor(module\_number);

\-- Foreign key de ticket a advisor (se agrega ahora que advisor existe)  
ALTER TABLE ticket  
    ADD CONSTRAINT fk\_ticket\_advisor   
    FOREIGN KEY (assigned\_advisor\_id)   
    REFERENCES advisor(id)   
    ON DELETE SET NULL;

\-- Datos iniciales: 5 asesores  
INSERT INTO advisor (name, email, status, module\_number) VALUES  
    ('María González', 'maria.gonzalez@institucion.cl', 'AVAILABLE', 1),  
    ('Juan Pérez', 'juan.perez@institucion.cl', 'AVAILABLE', 2),  
    ('Ana Silva', 'ana.silva@institucion.cl', 'AVAILABLE', 3),  
    ('Carlos Rojas', 'carlos.rojas@institucion.cl', 'AVAILABLE', 4),  
    ('Patricia Díaz', 'patricia.diaz@institucion.cl', 'AVAILABLE', 5);

\-- Comentarios  
COMMENT ON TABLE advisor IS 'Asesores/ejecutivos que atienden clientes';  
COMMENT ON COLUMN advisor.status IS 'Estado: AVAILABLE, BUSY, OFFLINE';  
COMMENT ON COLUMN advisor.module\_number IS 'Número de módulo de atención (1-5)';  
COMMENT ON COLUMN advisor.assigned\_tickets\_count IS 'Cantidad de tickets actualmente asignados';

---

## **PARTE 3: Configuración del Proyecto**

### **pom.xml (Maven)**

\<?xml version="1.0" encoding="UTF-8"?\>  
\<project xmlns="http://maven.apache.org/POM/4.0.0"  
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0   
         https://maven.apache.org/xsd/maven-4.0.0.xsd"\>  
    \<modelVersion\>4.0.0\</modelVersion\>

    \<parent\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-parent\</artifactId\>  
        \<version\>3.2.11\</version\>  
        \<relativePath/\>  
    \</parent\>

    \<groupId\>com.example\</groupId\>  
    \<artifactId\>ticketero\</artifactId\>  
    \<version\>1.0.0\</version\>  
    \<name\>Ticketero API\</name\>  
    \<description\>Sistema de Gestión de Tickets con Notificaciones en Tiempo Real\</description\>

    \<properties\>  
        \<java.version\>21\</java.version\>  
        \<maven.compiler.source\>21\</maven.compiler.source\>  
        \<maven.compiler.target\>21\</maven.compiler.target\>  
    \</properties\>

    \<dependencies\>  
        \<\!-- Spring Boot Starters \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-web\</artifactId\>  
        \</dependency\>

        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-data-jpa\</artifactId\>  
        \</dependency\>

        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-validation\</artifactId\>  
        \</dependency\>

        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-actuator\</artifactId\>  
        \</dependency\>

        \<\!-- PostgreSQL Driver \--\>  
        \<dependency\>  
            \<groupId\>org.postgresql\</groupId\>  
            \<artifactId\>postgresql\</artifactId\>  
            \<scope\>runtime\</scope\>  
        \</dependency\>

        \<\!-- Flyway for Database Migrations \--\>  
        \<dependency\>  
            \<groupId\>org.flywaydb\</groupId\>  
            \<artifactId\>flyway-core\</artifactId\>  
        \</dependency\>

        \<dependency\>  
            \<groupId\>org.flywaydb\</groupId\>  
            \<artifactId\>flyway-database-postgresql\</artifactId\>  
        \</dependency\>

        \<\!-- Lombok \--\>  
        \<dependency\>  
            \<groupId\>org.projectlombok\</groupId\>  
            \<artifactId\>lombok\</artifactId\>  
            \<optional\>true\</optional\>  
        \</dependency\>

        \<\!-- Testing \--\>  
        \<dependency\>  
            \<groupId\>org.springframework.boot\</groupId\>  
            \<artifactId\>spring-boot-starter-test\</artifactId\>  
            \<scope\>test\</scope\>  
        \</dependency\>

        \<dependency\>  
            \<groupId\>com.h2database\</groupId\>  
            \<artifactId\>h2\</artifactId\>  
            \<scope\>test\</scope\>  
        \</dependency\>  
    \</dependencies\>

    \<build\>  
        \<plugins\>  
            \<plugin\>  
                \<groupId\>org.springframework.boot\</groupId\>  
                \<artifactId\>spring-boot-maven-plugin\</artifactId\>  
                \<configuration\>  
                    \<excludes\>  
                        \<exclude\>  
                            \<groupId\>org.projectlombok\</groupId\>  
                            \<artifactId\>lombok\</artifactId\>  
                        \</exclude\>  
                    \</excludes\>  
                \</configuration\>  
            \</plugin\>  
        \</plugins\>  
    \</build\>  
\</project\>

---

### **application.yml**

spring:  
  application:  
    name: ticketero-api

  datasource:  
    url: ${DATABASE\_URL:jdbc:postgresql://localhost:5432/ticketero}  
    username: ${DATABASE\_USERNAME:dev}  
    password: ${DATABASE\_PASSWORD:dev123}  
    driver-class-name: org.postgresql.Driver

  jpa:  
    hibernate:  
      ddl-auto: validate  \# Flyway maneja el schema  
    show-sql: false  
    properties:  
      hibernate:  
        format\_sql: true  
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:  
    enabled: true  
    baseline-on-migrate: true  
    locations: classpath:db/migration

\# Telegram Configuration  
telegram:  
  bot-token: ${TELEGRAM\_BOT\_TOKEN}  
  api-url: https://api.telegram.org/bot

\# Actuator Endpoints  
management:  
  endpoints:  
    web:  
      exposure:  
        include: health,info,metrics  
  endpoint:  
    health:  
      show-details: when-authorized

\# Logging  
logging:  
  level:  
    com.example.ticketero: INFO  
    org.springframework: WARN  
    org.hibernate.SQL: DEBUG  
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  
  pattern:  
    console: "%d{yyyy-MM-dd HH:mm:ss} \- %msg%n"

---

### **.env (Template)**

\# Telegram Bot Configuration  
TELEGRAM\_BOT\_TOKEN=your\_telegram\_bot\_token\_here

\# Database Configuration  
DATABASE\_URL=jdbc:postgresql://localhost:5432/ticketero  
DATABASE\_USERNAME=dev  
DATABASE\_PASSWORD=dev123

\# Spring Profile  
SPRING\_PROFILES\_ACTIVE=dev

---

### **docker-compose.yml**

version: '3.8'

services:  
  postgres:  
    image: postgres:16-alpine  
    container\_name: ticketero-db  
    ports:  
      \- "5432:5432"  
    environment:  
      POSTGRES\_DB: ticketero  
      POSTGRES\_USER: dev  
      POSTGRES\_PASSWORD: dev123  
    volumes:  
      \- postgres\_data:/var/lib/postgresql/data  
    healthcheck:  
      test: \["CMD-SHELL", "pg\_isready \-U dev \-d ticketero"\]  
      interval: 10s  
      timeout: 5s  
      retries: 5

  api:  
    build:  
      context: .  
      dockerfile: Dockerfile  
    container\_name: ticketero-api  
    ports:  
      \- "8080:8080"  
    environment:  
      DATABASE\_URL: jdbc:postgresql://postgres:5432/ticketero  
      DATABASE\_USERNAME: dev  
      DATABASE\_PASSWORD: dev123  
      TELEGRAM\_BOT\_TOKEN: ${TELEGRAM\_BOT\_TOKEN}  
      SPRING\_PROFILES\_ACTIVE: dev  
    depends\_on:  
      postgres:  
        condition: service\_healthy  
    restart: unless-stopped

volumes:  
  postgres\_data:  
    driver: local

---

### **Dockerfile (Multi-stage)**

\# Stage 1: Build  
FROM maven:3.9-eclipse-temurin-21 AS build  
WORKDIR /app

\# Copy pom.xml and download dependencies (for caching)  
COPY pom.xml .  
RUN mvn dependency:go-offline \-B

\# Copy source code and build  
COPY src ./src  
RUN mvn clean package \-DskipTests

\# Stage 2: Runtime  
FROM eclipse-temurin:21-jre-alpine  
WORKDIR /app

\# Copy jar from build stage  
COPY \--from=build /app/target/\*.jar app.jar

\# Expose port  
EXPOSE 8080

\# Health check  
HEALTHCHECK \--interval=30s \--timeout=3s \--start-period=40s \--retries=3 \\  
  CMD wget \--no-verbose \--tries=1 \--spider http://localhost:8080/actuator/health || exit 1

\# Run application  
ENTRYPOINT \["java", "-jar", "app.jar"\]

---

## **PARTE 4: Checklist de Implementación por Fases**

### **Fase 0: Setup del Proyecto (30 minutos)**

**Objetivo:** Configurar el proyecto base y verificar que compila

**Tareas:**

* \[ \] Crear proyecto Maven con estructura de carpetas  
* \[ \] Configurar `pom.xml` con todas las dependencias  
* \[ \] Crear `application.yml` con configuración base  
* \[ \] Crear `.env` con variables de entorno  
* \[ \] Crear `docker-compose.yml` para PostgreSQL  
* \[ \] Levantar base de datos: `docker-compose up -d postgres`  
* \[ \] Crear clase principal `TicketeroApplication.java` con `@SpringBootApplication` y `@EnableScheduling`  
* \[ \] Verificar compilación: `mvn clean compile`  
* \[ \] Verificar que conecta a BD: `mvn spring-boot:run`

**Criterios de Aceptación:**

* ✅ Proyecto compila sin errores  
* ✅ Aplicación inicia y conecta a PostgreSQL  
* ✅ Logs muestran: "Started TicketeroApplication"  
* ✅ Actuator health endpoint responde: `curl http://localhost:8080/actuator/health`

---

### **Fase 1: Migraciones y Enumeraciones (45 minutos)**

**Objetivo:** Crear esquema de base de datos y enumeraciones Java

**Tareas:**

* \[ \] Crear `V1__create_ticket_table.sql`  
* \[ \] Crear `V2__create_mensaje_table.sql`  
* \[ \] Crear `V3__create_advisor_table.sql`  
* \[ \] Crear enum `QueueType.java`  
* \[ \] Crear enum `TicketStatus.java`  
* \[ \] Crear enum `AdvisorStatus.java`  
* \[ \] Crear enum `MessageTemplate.java`  
* \[ \] Reiniciar aplicación y verificar migraciones  
* \[ \] Verificar tablas creadas: `\dt` en psql  
* \[ \] Verificar datos iniciales: `SELECT * FROM advisor;`

**Criterios de Aceptación:**

* ✅ Flyway ejecuta las 3 migraciones exitosamente  
* ✅ Tabla `flyway_schema_history` muestra 3 versiones  
* ✅ Tablas `ticket`, `mensaje`, `advisor` existen  
* ✅ 5 asesores iniciales insertados en `advisor`  
* ✅ 4 enums creadas con valores correctos

**Ejemplo de Enum:**

package com.example.ticketero.model.enums;

public enum QueueType {  
    CAJA("Caja", 5, 1),  
    PERSONAL\_BANKER("Personal Banker", 15, 2),  
    EMPRESAS("Empresas", 20, 3),  
    GERENCIA("Gerencia", 30, 4);

    private final String displayName;  
    private final int avgTimeMinutes;  
    private final int priority;

    QueueType(String displayName, int avgTimeMinutes, int priority) {  
        this.displayName \= displayName;  
        this.avgTimeMinutes \= avgTimeMinutes;  
        this.priority \= priority;  
    }

    public String getDisplayName() { return displayName; }  
    public int getAvgTimeMinutes() { return avgTimeMinutes; }  
    public int getPriority() { return priority; }  
}

---

### **Fase 2: Entities (1 hora)**

**Objetivo:** Crear las 3 entidades JPA mapeadas a las tablas

**Tareas:**

* \[ \] Crear `Ticket.java` con todas las anotaciones JPA  
* \[ \] Crear `Mensaje.java` con relación a Ticket  
* \[ \] Crear `Advisor.java` con relación a Ticket  
* \[ \] Usar Lombok: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`  
* \[ \] Mapear enums con `@Enumerated(EnumType.STRING)`  
* \[ \] Configurar relaciones: `@OneToMany`, `@ManyToOne`  
* \[ \] Agregar `@PrePersist` para `codigo_referencia` UUID  
* \[ \] Compilar y verificar sin errores

**Criterios de Aceptación:**

* ✅ 3 entities creadas con anotaciones JPA correctas  
* ✅ Relaciones bidireccionales configuradas  
* ✅ Proyecto compila sin errores  
* ✅ Hibernate valida el schema al iniciar (no crea tablas por `ddl-auto=validate`)

**Ejemplo de Entity:**

package com.example.ticketero.model.entity;

import jakarta.persistence.\*;  
import lombok.\*;  
import java.time.LocalDateTime;  
import java.util.UUID;

@Entity  
@Table(name \= "ticket")  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
@Builder  
public class Ticket {  
      
    @Id  
    @GeneratedValue(strategy \= GenerationType.IDENTITY)  
    private Long id;

    @Column(name \= "codigo\_referencia", nullable \= false, unique \= true)  
    private UUID codigoReferencia;

    @Column(name \= "numero", nullable \= false, unique \= true, length \= 10\)  
    private String numero;

    @Column(name \= "national\_id", nullable \= false, length \= 20\)  
    private String nationalId;

    @Column(name \= "telefono", length \= 20\)  
    private String telefono;

    @Column(name \= "branch\_office", nullable \= false, length \= 100\)  
    private String branchOffice;

    @Enumerated(EnumType.STRING)  
    @Column(name \= "queue\_type", nullable \= false, length \= 20\)  
    private QueueType queueType;

    @Enumerated(EnumType.STRING)  
    @Column(name \= "status", nullable \= false, length \= 20\)  
    private TicketStatus status;

    @Column(name \= "position\_in\_queue", nullable \= false)  
    private Integer positionInQueue;

    @Column(name \= "estimated\_wait\_minutes", nullable \= false)  
    private Integer estimatedWaitMinutes;

    @ManyToOne(fetch \= FetchType.LAZY)  
    @JoinColumn(name \= "assigned\_advisor\_id")  
    private Advisor assignedAdvisor;

    @Column(name \= "assigned\_module\_number")  
    private Integer assignedModuleNumber;

    @Column(name \= "created\_at", nullable \= false, updatable \= false)  
    private LocalDateTime createdAt;

    @Column(name \= "updated\_at", nullable \= false)  
    private LocalDateTime updatedAt;

    @PrePersist  
    protected void onCreate() {  
        codigoReferencia \= UUID.randomUUID();  
        createdAt \= LocalDateTime.now();  
        updatedAt \= LocalDateTime.now();  
    }

    @PreUpdate  
    protected void onUpdate() {  
        updatedAt \= LocalDateTime.now();  
    }  
}

---

### **Fase 3: DTOs (45 minutos)**

**Objetivo:** Crear DTOs para request/response

**Tareas:**

* \[ \] Crear `TicketCreateRequest.java` con Bean Validation  
* \[ \] Crear `TicketResponse.java` como record  
* \[ \] Crear `QueuePositionResponse.java`  
* \[ \] Crear `DashboardResponse.java`  
* \[ \] Crear `QueueStatusResponse.java`  
* \[ \] Agregar validaciones: `@NotBlank`, `@NotNull`, `@Pattern`  
* \[ \] Compilar y verificar

**Criterios de Aceptación:**

* ✅ 5 DTOs creados  
* ✅ Validaciones Bean Validation configuradas  
* ✅ Records usados donde sea apropiado (inmutabilidad)

**Ejemplo de DTO:**

package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;  
import jakarta.validation.constraints.NotBlank;  
import jakarta.validation.constraints.NotNull;  
import jakarta.validation.constraints.Pattern;

public record TicketCreateRequest(  
      
    @NotBlank(message \= "El RUT/ID es obligatorio")  
    String nationalId,  
      
    @Pattern(regexp \= "^\\\\+56\[0-9\]{9}$", message \= "Teléfono debe tener formato \+56XXXXXXXXX")  
    String telefono,  
      
    @NotBlank(message \= "La sucursal es obligatoria")  
    String branchOffice,  
      
    @NotNull(message \= "El tipo de cola es obligatorio")  
    QueueType queueType  
) {}

---

### **Fase 4: Repositories (30 minutos)**

**Objetivo:** Crear interfaces de acceso a datos

**Tareas:**

* \[ \] Crear `TicketRepository.java` extends JpaRepository  
* \[ \] Crear `MensajeRepository.java`  
* \[ \] Crear `AdvisorRepository.java`  
* \[ \] Agregar queries custom con `@Query`  
* \[ \] Métodos: findByCodigoReferencia, findByNationalIdAndStatusIn, etc.

**Criterios de Aceptación:**

* ✅ 3 repositories creados  
* ✅ Queries custom documentadas  
* ✅ Proyecto compila

**Ejemplo:**

package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Ticket;  
import com.example.ticketero.model.enums.TicketStatus;  
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.data.jpa.repository.Query;  
import org.springframework.data.repository.query.Param;  
import org.springframework.stereotype.Repository;

import java.util.List;  
import java.util.Optional;  
import java.util.UUID;

@Repository  
public interface TicketRepository extends JpaRepository\<Ticket, Long\> {

    Optional\<Ticket\> findByCodigoReferencia(UUID codigoReferencia);

    Optional\<Ticket\> findByNumero(String numero);

    @Query("SELECT t FROM Ticket t WHERE t.nationalId \= :nationalId AND t.status IN :statuses")  
    Optional\<Ticket\> findByNationalIdAndStatusIn(  
        @Param("nationalId") String nationalId,   
        @Param("statuses") List\<TicketStatus\> statuses  
    );

    @Query("SELECT t FROM Ticket t WHERE t.status \= :status ORDER BY t.createdAt ASC")  
    List\<Ticket\> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);  
}

---

### **Fase 5: Services (3 horas)**

**Objetivo:** Implementar toda la lógica de negocio

**Tareas:**

* \[ \] Crear `TelegramService.java` (envío de mensajes)  
* \[ \] Crear `TicketService.java` (crear ticket, calcular posición)  
* \[ \] Crear `QueueManagementService.java` (asignación automática)  
* \[ \] Crear `AdvisorService.java` (gestión de asesores)  
* \[ \] Crear `NotificationService.java` (coordinar notificaciones)  
* \[ \] Implementar lógica según RN-001 a RN-013  
* \[ \] Agregar `@Transactional` donde corresponda  
* \[ \] Logging con `@Slf4j`

**Orden de Implementación:**

1. TelegramService (sin dependencias)  
2. AdvisorService (solo repository)  
3. TicketService (usa TelegramService)  
4. QueueManagementService (usa TicketService, AdvisorService)  
5. NotificationService (usa TelegramService)

**Criterios de Aceptación:**

* ✅ 5 services implementados  
* ✅ Reglas de negocio RN-001 a RN-013 aplicadas  
* ✅ Transacciones configuradas correctamente  
* ✅ Tests unitarios básicos pasan

**Ejemplo:**

package com.example.ticketero.service;

import com.example.ticketero.model.dto.TicketCreateRequest;  
import com.example.ticketero.model.dto.TicketResponse;  
import com.example.ticketero.model.entity.Ticket;  
import com.example.ticketero.model.enums.TicketStatus;  
import com.example.ticketero.repository.TicketRepository;  
import lombok.RequiredArgsConstructor;  
import lombok.extern.slf4j.Slf4j;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service  
@RequiredArgsConstructor  
@Slf4j  
@Transactional(readOnly \= true)  
public class TicketService {

    private final TicketRepository ticketRepository;  
    private final MensajeRepository mensajeRepository;

    @Transactional  
    public TicketResponse crearTicket(TicketCreateRequest request) {  
        log.info("Creando ticket para nationalId: {}", request.nationalId());

        // RN-001: Validar ticket activo existente  
        validarTicketActivoExistente(request.nationalId());

        // Generar número según RN-005, RN-006  
        String numero \= generarNumeroTicket(request.queueType());

        // Calcular posición según RN-010  
        int posicion \= calcularPosicionEnCola(request.queueType());  
        int tiempoEstimado \= calcularTiempoEstimado(posicion, request.queueType());

        // Crear y guardar ticket  
        Ticket ticket \= Ticket.builder()  
            .nationalId(request.nationalId())  
            .telefono(request.telefono())  
            .branchOffice(request.branchOffice())  
            .queueType(request.queueType())  
            .status(TicketStatus.EN\_ESPERA)  
            .positionInQueue(posicion)  
            .estimatedWaitMinutes(tiempoEstimado)  
            .build();

        ticket \= ticketRepository.save(ticket);

        // Programar 3 mensajes (si hay teléfono)  
        if (request.telefono() \!= null && \!request.telefono().isBlank()) {  
            programarMensajes(ticket);  
        }

        log.info("Ticket creado: {}", ticket.getNumero());

        return toResponse(ticket);  
    }

    private void validarTicketActivoExistente(String nationalId) {  
        List\<TicketStatus\> estadosActivos \= List.of(  
            TicketStatus.EN\_ESPERA,   
            TicketStatus.PROXIMO,   
            TicketStatus.ATENDIENDO  
        );  
          
        ticketRepository.findByNationalIdAndStatusIn(nationalId, estadosActivos)  
            .ifPresent(t \-\> {  
                throw new TicketActivoExistenteException(  
                    "Ya tienes un ticket activo: " \+ t.getNumero()  
                );  
            });  
    }

    // ... otros métodos privados  
}

---

### **Fase 6: Controllers (2 horas)**

**Objetivo:** Exponer API REST

**Tareas:**

* \[ \] Crear `TicketController.java` (endpoints públicos)  
* \[ \] Crear `AdminController.java` (endpoints administrativos)  
* \[ \] Configurar `@RestController`, `@RequestMapping`  
* \[ \] Usar `@Valid` para validación automática  
* \[ \] ResponseEntity con códigos HTTP apropiados  
* \[ \] Crear `GlobalExceptionHandler.java` para errores

**Endpoints a Implementar:**

**TicketController:**

* POST /api/tickets \- Crear ticket  
* GET /api/tickets/{uuid} \- Obtener ticket  
* GET /api/tickets/{numero}/position \- Consultar posición  
* GET /api/health \- Health check

**AdminController:**

* GET /api/admin/dashboard \- Dashboard completo  
* GET /api/admin/queues/{type} \- Estado de cola  
* GET /api/admin/queues/{type}/stats \- Estadísticas  
* GET /api/admin/advisors \- Lista asesores  
* GET /api/admin/advisors/stats \- Estadísticas asesores  
* PUT /api/admin/advisors/{id}/status \- Cambiar estado  
* GET /api/admin/summary \- Resumen ejecutivo

**Criterios de Aceptación:**

* ✅ 11 endpoints implementados  
* ✅ Validación automática funciona  
* ✅ Manejo de errores centralizado  
* ✅ Códigos HTTP correctos (200, 201, 400, 404, 409\)

**Ejemplo:**

package com.example.ticketero.controller;

import com.example.ticketero.model.dto.TicketCreateRequest;  
import com.example.ticketero.model.dto.TicketResponse;  
import com.example.ticketero.service.TicketService;  
import jakarta.validation.Valid;  
import lombok.RequiredArgsConstructor;  
import lombok.extern.slf4j.Slf4j;  
import org.springframework.http.HttpStatus;  
import org.springframework.http.ResponseEntity;  
import org.springframework.web.bind.annotation.\*;

import java.util.UUID;

@RestController  
@RequestMapping("/api/tickets")  
@RequiredArgsConstructor  
@Slf4j  
public class TicketController {

    private final TicketService ticketService;

    @PostMapping  
    public ResponseEntity\<TicketResponse\> crearTicket(  
        @Valid @RequestBody TicketCreateRequest request  
    ) {  
        log.info("POST /api/tickets \- Creando ticket para {}", request.nationalId());  
          
        TicketResponse response \= ticketService.crearTicket(request);  
          
        return ResponseEntity  
            .status(HttpStatus.CREATED)  
            .body(response);  
    }

    @GetMapping("/{codigoReferencia}")  
    public ResponseEntity\<TicketResponse\> obtenerTicket(  
        @PathVariable UUID codigoReferencia  
    ) {  
        log.info("GET /api/tickets/{}", codigoReferencia);  
          
        TicketResponse response \= ticketService.obtenerTicketPorCodigo(codigoReferencia);  
          
        return ResponseEntity.ok(response);  
    }  
}

---

### **Fase 7: Schedulers (1.5 horas)**

**Objetivo:** Implementar procesamiento asíncrono

**Tareas:**

* \[ \] Crear `MensajeScheduler.java` (@Scheduled fixedRate=60000)  
* \[ \] Crear `QueueProcessorScheduler.java` (@Scheduled fixedRate=5000)  
* \[ \] Configurar `@EnableScheduling` en clase principal  
* \[ \] Implementar lógica de reintentos (RN-007, RN-008)  
* \[ \] Implementar asignación automática (RN-002, RN-003, RN-004)  
* \[ \] Logging detallado

**Criterios de Aceptación:**

* ✅ MensajeScheduler procesa mensajes pendientes cada 60s  
* ✅ QueueProcessorScheduler asigna tickets cada 5s  
* ✅ Reintentos funcionan (30s, 60s, 120s backoff)  
* ✅ Asignación respeta prioridades y FIFO

**Ejemplo:**

package com.example.ticketero.scheduler;

import com.example.ticketero.model.entity.Mensaje;  
import com.example.ticketero.repository.MensajeRepository;  
import com.example.ticketero.service.TelegramService;  
import lombok.RequiredArgsConstructor;  
import lombok.extern.slf4j.Slf4j;  
import org.springframework.scheduling.annotation.Scheduled;  
import org.springframework.stereotype.Component;  
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;  
import java.util.List;

@Component  
@RequiredArgsConstructor  
@Slf4j  
public class MensajeScheduler {

    private final MensajeRepository mensajeRepository;  
    private final TelegramService telegramService;

    @Scheduled(fixedRate \= 60000\) // Cada 60 segundos  
    @Transactional  
    public void procesarMensajesPendientes() {  
        LocalDateTime ahora \= LocalDateTime.now();

        List\<Mensaje\> mensajesPendientes \= mensajeRepository  
            .findByEstadoEnvioAndFechaProgramadaLessThanEqual("PENDIENTE", ahora);

        if (mensajesPendientes.isEmpty()) {  
            log.debug("No hay mensajes pendientes");  
            return;  
        }

        log.info("Procesando {} mensajes pendientes", mensajesPendientes.size());

        for (Mensaje mensaje : mensajesPendientes) {  
            try {  
                enviarMensaje(mensaje);  
            } catch (Exception e) {  
                log.error("Error procesando mensaje {}: {}", mensaje.getId(), e.getMessage());  
            }  
        }  
    }

    private void enviarMensaje(Mensaje mensaje) {  
        // Implementación de envío con manejo de reintentos (RN-007, RN-008)  
    }  
}

---

## **PARTE 5: Orden de Ejecución Recomendado**

Día 1 (4 horas):  
├── Fase 0: Setup (30 min)  
├── Fase 1: Migraciones (45 min)  
├── Fase 2: Entities (1 hora)  
├── Fase 3: DTOs (45 min)  
└── Fase 4: Repositories (30 min)

Día 2 (5 horas):  
├── Fase 5: Services (3 horas)  
├── Fase 6: Controllers (2 horas)

Día 3 (2 horas):  
└── Fase 7: Schedulers (1.5 horas)  
└── Testing E2E (30 min)

TOTAL: \~11 horas de implementación

---

## **PARTE 6: Comandos Útiles**

### **Maven**

\# Compilar  
mvn clean compile

\# Ejecutar tests  
mvn test

\# Empaquetar (sin tests)  
mvn clean package \-DskipTests

\# Ejecutar aplicación  
mvn spring-boot:run

### **Docker**

\# Levantar PostgreSQL solo  
docker-compose up \-d postgres

\# Ver logs  
docker-compose logs \-f postgres

\# Levantar todo (PostgreSQL \+ API)  
docker-compose up \--build

\# Detener y limpiar  
docker-compose down \-v

### **PostgreSQL**

\# Conectar a base de datos  
docker exec \-it ticketero-db psql \-U dev \-d ticketero

\# Ver tablas  
\\dt

\# Ver migraciones  
SELECT \* FROM flyway\_schema\_history;

\# Ver asesores  
SELECT \* FROM advisor;

### **Testing Manual**

\# Health check  
curl http://localhost:8080/actuator/health

\# Crear ticket  
curl \-X POST http://localhost:8080/api/tickets \\  
  \-H "Content-Type: application/json" \\  
  \-d '{  
    "nationalId": "12345678-9",  
    "telefono": "+56912345678",  
    "branchOffice": "Sucursal Centro",  
    "queueType": "PERSONAL\_BANKER"  
  }' | jq

\# Obtener dashboard  
curl http://localhost:8080/api/admin/dashboard | jq

---

## **PARTE 7: Estructura del Documento Final**

El documento de plan debe tener esta estructura:

\# Plan Detallado de Implementación \- Sistema Ticketero

\*\*Proyecto:\*\* Sistema de Gestión de Tickets con Notificaciones en Tiempo Real    
\*\*Versión:\*\* 1.0    
\*\*Fecha:\*\* Diciembre 2025    
\*\*Tech Lead:\*\* \[Nombre\]

\---

\#\# 1\. Introducción  
\[Descripción del plan, objetivo, tiempo estimado\]

\#\# 2\. Estructura del Proyecto  
\[Árbol de carpetas completo\]

\#\# 3\. Configuración Inicial  
\[pom.xml, application.yml, .env, docker-compose, Dockerfile\]

\#\# 4\. Migraciones de Base de Datos  
\[3 archivos SQL de Flyway\]

\#\# 5\. Implementación por Fases  
\[Fases 0-7 con checklists detallados\]

\#\# 6\. Orden de Ejecución Recomendado  
\[Cronograma de 3 días\]

\#\# 7\. Comandos Útiles  
\[Maven, Docker, PostgreSQL, Testing\]

\#\# 8\. Troubleshooting  
\[Problemas comunes y soluciones\]

\#\# 9\. Checklist Final de Validación  
\[Criterios para considerar el proyecto completo\]

---

## **Criterios de Calidad**

Tu documento DEBE cumplir:

**✅ Completitud:**

* \[ \] Estructura de proyecto completa (42+ archivos Java)  
* \[ \] 3 migraciones SQL con DDL completo  
* \[ \] Configuración completa (pom.xml, application.yml, docker-compose, Dockerfile)  
* \[ \] 8 fases documentadas con checklists  
* \[ \] Orden de implementación claro

**✅ Claridad:**

* \[ \] Cada fase tiene objetivo, tareas y criterios de aceptación  
* \[ \] Ejemplos de código para patrones clave  
* \[ \] Comandos copy-paste listos

**✅ Ejecutabilidad:**

* \[ \] Un desarrollador puede seguir el plan sin consultar otros documentos  
* \[ \] Tiempo estimado realista (11 horas)  
* \[ \] Criterios de aceptación verificables

**✅ Alineación con Arquitectura:**

* \[ \] Stack tecnológico coincide con ARQUITECTURA.md  
* \[ \] Componentes coinciden con diagrama de secuencia  
* \[ \] Entidades coinciden con modelo ER

---

## **Restricciones**

**❌ NO incluir:**

* Código Java completo de todas las clases (eso es PROMPT 4\)  
* Solo incluir ejemplos representativos

**✅ SÍ incluir:**

* Estructura completa de archivos  
* Migraciones SQL completas  
* Configuración completa (pom.xml, yml, docker)  
* Checklists detallados por fase  
* Ejemplos de código para patrones clave

---

## **Entregable**

**Archivo:** `PLAN-IMPLEMENTACION.md`  
**Ubicación:** `docs/`  
**Longitud esperada:** 40-50 páginas (10,000-12,000 palabras)

Este documento será la entrada para:

* PROMPT 4: Implementación de Código Completo  
* Desarrollo por parte del equipo  
* Revisión de avance por fases

---

**IMPORTANTE:** El plan debe ser **ejecutable paso a paso** por un desarrollador mid-level sin necesidad de consultar documentación adicional. Cada fase debe ser independiente y verificable.


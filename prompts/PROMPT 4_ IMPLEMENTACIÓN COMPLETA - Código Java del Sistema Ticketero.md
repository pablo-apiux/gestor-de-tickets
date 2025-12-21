# **PROMPT 4: IMPLEMENTACIÓN COMPLETA \- Código Java del Sistema Ticketero**

## **Contexto**

Eres un Desarrollador Senior Full-Stack con expertise en Java 21 y Spring Boot. Has recibido el Plan Detallado de Implementación aprobado y tu tarea es implementar **TODO el código Java del sistema** siguiendo las especificaciones exactas.

**IMPORTANTE:** Después de completar CADA paso, debes DETENERTE y solicitar una **revisión exhaustiva** antes de continuar con el siguiente paso.

---

## **Documentos de Entrada**

**Lee estos archivos que YA están en tu proyecto:**

1. `docs/REQUERIMIENTOS-NEGOCIO.md` \- Contexto de negocio  
2. `REQUERIMIENTOS-FUNCIONALES.md` \- RF-001 a RF-008 con criterios de aceptación  
3. `docs/ARQUITECTURA.md` \- Stack tecnológico, diagramas, componentes, ADRs  
4. `PLAN-IMPLEMENTACION.md` \- Estructura de proyecto, migraciones, fases

**CRÍTICO:** Referencia el código existente en el proyecto GitHub como "ground truth" para patrones y estilo de código.

---

## **Metodología de Implementación**

### **Principio Fundamental:**

**"Implementar → Validar → Confirmar → Continuar"**

Después de CADA paso:

1. ✅ Implementa el código completo del paso  
2. ✅ Compila y verifica que no hay errores  
3. ✅ Ejecuta validaciones específicas del paso  
4. ⏸️ **DETENTE y solicita revisión exhaustiva**  
5. ✅ Espera confirmación antes de continuar

### **Formato de Solicitud de Revisión:**

Después de cada paso, DEBES decir:

✅ PASO X COMPLETADO

Archivos creados/modificados:  
\- \[lista de archivos\]

Validaciones realizadas:  
\- \[checklist de validaciones\]

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿El código compila sin errores?  
2\. ¿Se siguen los patrones del proyecto?  
3\. ¿Las anotaciones son correctas?  
4\. ¿Falta algún archivo o configuración?  
5\. ¿Puedo continuar con el siguiente paso?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **FASE 0: Setup del Proyecto**

### **PASO 0.1: Crear Estructura Base de Maven**

**Tareas:**

* Crear `pom.xml` con todas las dependencias  
* Verificar que Maven descarga dependencias correctamente  
* Compilar proyecto vacío

**Implementación:**

Crea el archivo `pom.xml` en la raíz del proyecto:

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
        \<project.build.sourceEncoding\>UTF-8\</project.build.sourceEncoding\>  
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

**Validaciones:**

\# 1\. Verificar que Maven puede leer el pom.xml  
mvn validate

\# 2\. Descargar todas las dependencias  
mvn dependency:resolve

\# 3\. Compilar (debe fallar porque no hay código aún, pero sin errores de dependencias)  
mvn clean compile

**🔍 PUNTO DE REVISIÓN 0.1:**

Después de crear el `pom.xml`, DETENTE y solicita revisión:

✅ PASO 0.1 COMPLETADO

Archivos creados:  
\- pom.xml

Validaciones realizadas:  
\- ✅ mvn validate ejecutado exitosamente  
\- ✅ mvn dependency:resolve descargó todas las dependencias  
\- ✅ No hay errores de dependencias

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿Las versiones de dependencias son correctas?  
2\. ¿Falta alguna dependencia crítica?  
3\. ¿El plugin de Spring Boot está configurado correctamente?  
4\. ¿La configuración de Lombok está correcta?  
5\. ¿Puedo continuar con el PASO 0.2?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

### **PASO 0.2: Configuración de Spring Boot y Docker**

**Tareas:**

* Crear `application.yml` con configuración base  
* Crear `.env` template  
* Crear `docker-compose.yml`  
* Crear `Dockerfile`

**Implementación:**

**Archivo 1:** `src/main/resources/application.yml`

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
  bot-token: ${TELEGRAM\_BOT\_TOKEN:}  
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

**Archivo 2:** `.env` (template)

\# Telegram Bot Configuration  
TELEGRAM\_BOT\_TOKEN=your\_telegram\_bot\_token\_here

\# Database Configuration  
DATABASE\_URL=jdbc:postgresql://localhost:5432/ticketero  
DATABASE\_USERNAME=dev  
DATABASE\_PASSWORD=dev123

\# Spring Profile  
SPRING\_PROFILES\_ACTIVE=dev

**Archivo 3:** `docker-compose.yml`

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

**Archivo 4:** `Dockerfile`

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

**Validaciones:**

\# 1\. Levantar solo PostgreSQL  
docker-compose up \-d postgres

\# 2\. Verificar que PostgreSQL está corriendo  
docker-compose ps

\# 3\. Verificar logs de PostgreSQL  
docker-compose logs postgres | grep "database system is ready to accept connections"

**🔍 PUNTO DE REVISIÓN 0.2:**

✅ PASO 0.2 COMPLETADO

Archivos creados:  
\- src/main/resources/application.yml  
\- .env  
\- docker-compose.yml  
\- Dockerfile

Validaciones realizadas:  
\- ✅ docker-compose up \-d postgres ejecutado  
\- ✅ PostgreSQL está corriendo (docker-compose ps)  
\- ✅ PostgreSQL acepta conexiones (logs verificados)

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿La configuración de application.yml es correcta?  
2\. ¿Las variables de entorno en docker-compose son correctas?  
3\. ¿El Dockerfile usa multi-stage build correctamente?  
4\. ¿PostgreSQL está accesible en puerto 5432?  
5\. ¿Puedo continuar con el PASO 0.3?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

### **PASO 0.3: Crear Clase Principal de Spring Boot**

**Tareas:**

* Crear `TicketeroApplication.java`  
* Habilitar scheduling con `@EnableScheduling`  
* Compilar y ejecutar para verificar conexión a BD

**Implementación:**

**Archivo:** `src/main/java/com/example/ticketero/TicketeroApplication.java`

package com.example.ticketero;

import org.springframework.boot.SpringApplication;  
import org.springframework.boot.autoconfigure.SpringBootApplication;  
import org.springframework.scheduling.annotation.EnableScheduling;

/\*\*  
 \* Clase principal de la aplicación Ticketero  
 \* Sistema de gestión de tickets con notificaciones en tiempo real  
 \*/  
@SpringBootApplication  
@EnableScheduling  
public class TicketeroApplication {

    public static void main(String\[\] args) {  
        SpringApplication.run(TicketeroApplication.class, args);  
    }  
}

**Validaciones:**

\# 1\. Compilar  
mvn clean compile

\# 2\. Ejecutar (debe fallar en migraciones Flyway porque no existen aún, pero debe conectar a BD)  
mvn spring-boot:run

\# Esperar estos logs:  
\# \- "Starting TicketeroApplication"  
\# \- "HikariPool-1 \- Starting..." (conexión a BD)  
\# \- Error de Flyway (esperado porque no hay migraciones aún)

\# 3\. Detener con Ctrl+C

**🔍 PUNTO DE REVISIÓN 0.3:**

✅ PASO 0.3 COMPLETADO

Archivos creados:  
\- src/main/java/com/example/ticketero/TicketeroApplication.java

Validaciones realizadas:  
\- ✅ mvn clean compile ejecutado sin errores  
\- ✅ mvn spring-boot:run inicia la aplicación  
\- ✅ Conexión a PostgreSQL exitosa (HikariPool logs)  
\- ✅ @EnableScheduling está configurado

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿La clase principal tiene todas las anotaciones necesarias?  
2\. ¿@EnableScheduling está presente para los schedulers?  
3\. ¿La aplicación se conecta correctamente a PostgreSQL?  
4\. ¿Los logs muestran "Starting TicketeroApplication"?  
5\. ¿Puedo continuar con FASE 1?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR A FASE 1...

---

## **FASE 1: Migraciones y Enumeraciones**

### **PASO 1.1: Crear Migraciones SQL de Flyway**

**Tareas:**

* Crear las 3 migraciones SQL  
* Verificar que Flyway las ejecuta correctamente  
* Validar que las tablas se crean con índices

**Implementación:**

**Archivo 1:** `src/main/resources/db/migration/V1__create_ticket_table.sql`

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

**Archivo 2:** `src/main/resources/db/migration/V2__create_mensaje_table.sql`

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

**Archivo 3:** `src/main/resources/db/migration/V3__create_advisor_table.sql`

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

**Validaciones:**

\# 1\. Ejecutar aplicación (Flyway ejecutará migraciones automáticamente)  
mvn spring-boot:run

\# 2\. Verificar en logs:  
\# \- "Flyway Community Edition"  
\# \- "Migrating schema ... to version 1"  
\# \- "Migrating schema ... to version 2"  
\# \- "Migrating schema ... to version 3"  
\# \- "Successfully applied 3 migrations"

\# 3\. Conectar a base de datos y verificar  
docker exec \-it ticketero-db psql \-U dev \-d ticketero

\# En psql:  
\\dt                                    \# Ver tablas  
SELECT \* FROM flyway\_schema\_history;   \# Ver migraciones  
SELECT \* FROM advisor;                 \# Ver 5 asesores  
\\d ticket                              \# Ver estructura de tabla ticket  
\\q                                     \# Salir

**🔍 PUNTO DE REVISIÓN 1.1:**

✅ PASO 1.1 COMPLETADO

Archivos creados:  
\- src/main/resources/db/migration/V1\_\_create\_ticket\_table.sql  
\- src/main/resources/db/migration/V2\_\_create\_mensaje\_table.sql  
\- src/main/resources/db/migration/V3\_\_create\_advisor\_table.sql

Validaciones realizadas:  
\- ✅ Flyway ejecutó las 3 migraciones exitosamente  
\- ✅ flyway\_schema\_history muestra 3 versiones  
\- ✅ Tablas ticket, mensaje, advisor creadas  
\- ✅ Índices creados correctamente  
\- ✅ 5 asesores insertados en advisor  
\- ✅ Foreign keys configuradas correctamente

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿Las 3 migraciones SQL son correctas?  
2\. ¿Los índices están en las columnas apropiadas?  
3\. ¿Los foreign keys tienen ON DELETE correcto?  
4\. ¿Los 5 asesores tienen datos válidos?  
5\. ¿Los constraints (CHECK) son correctos?  
6\. ¿Puedo continuar con el PASO 1.2?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

### **PASO 1.2: Crear Enumeraciones Java**

**Tareas:**

* Crear las 4 enumeraciones  
* Cada enum debe tener métodos útiles  
* Compilar y verificar

**Implementación:**

**Archivo 1:** `src/main/java/com/example/ticketero/model/enums/QueueType.java`

package com.example.ticketero.model.enums;

/\*\*  
 \* Tipos de cola disponibles en el sistema  
 \* Cada cola tiene tiempo promedio y prioridad diferente  
 \*/  
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

    public String getDisplayName() {  
        return displayName;  
    }

    public int getAvgTimeMinutes() {  
        return avgTimeMinutes;  
    }

    public int getPriority() {  
        return priority;  
    }

    /\*\*  
     \* Obtiene el prefijo para el número de ticket  
     \* @return C, P, E, o G según el tipo de cola  
     \*/  
    public char getPrefix() {  
        return switch (this) {  
            case CAJA \-\> 'C';  
            case PERSONAL\_BANKER \-\> 'P';  
            case EMPRESAS \-\> 'E';  
            case GERENCIA \-\> 'G';  
        };  
    }  
}

**Archivo 2:** `src/main/java/com/example/ticketero/model/enums/TicketStatus.java`

package com.example.ticketero.model.enums;

import java.util.List;

/\*\*  
 \* Estados posibles de un ticket  
 \*/  
public enum TicketStatus {  
    EN\_ESPERA,      // Esperando asignación  
    PROXIMO,        // Próximo a ser atendido (posición \<= 3\)  
    ATENDIENDO,     // Siendo atendido por un asesor  
    COMPLETADO,     // Atención finalizada  
    CANCELADO,      // Cancelado por cliente o sistema  
    NO\_ATENDIDO;    // Cliente no se presentó

    /\*\*  
     \* Estados considerados "activos" (cliente aún no ha sido atendido completamente)  
     \*/  
    public static List\<TicketStatus\> getActiveStatuses() {  
        return List.of(EN\_ESPERA, PROXIMO, ATENDIENDO);  
    }

    /\*\*  
     \* Verifica si este estado es considerado activo  
     \*/  
    public boolean isActive() {  
        return getActiveStatuses().contains(this);  
    }  
}

**Archivo 3:** `src/main/java/com/example/ticketero/model/enums/AdvisorStatus.java`

package com.example.ticketero.model.enums;

/\*\*  
 \* Estados posibles de un asesor  
 \*/  
public enum AdvisorStatus {  
    AVAILABLE,  // Disponible para atender  
    BUSY,       // Atendiendo un cliente  
    OFFLINE;    // No disponible (almuerzo, capacitación, etc.)

    /\*\*  
     \* Verifica si el asesor puede recibir asignaciones  
     \*/  
    public boolean canReceiveAssignments() {  
        return this \== AVAILABLE;  
    }  
}

**Archivo 4:** `src/main/java/com/example/ticketero/model/enums/MessageTemplate.java`

package com.example.ticketero.model.enums;

/\*\*  
 \* Plantillas de mensajes para Telegram  
 \*/  
public enum MessageTemplate {  
    TOTEM\_TICKET\_CREADO("totem\_ticket\_creado"),  
    TOTEM\_PROXIMO\_TURNO("totem\_proximo\_turno"),  
    TOTEM\_ES\_TU\_TURNO("totem\_es\_tu\_turno");

    private final String templateName;

    MessageTemplate(String templateName) {  
        this.templateName \= templateName;  
    }

    public String getTemplateName() {  
        return templateName;  
    }  
}

**Validaciones:**

\# 1\. Compilar  
mvn clean compile

\# 2\. Verificar que compila sin errores

\# 3\. Verificar que las clases están en target/classes  
ls \-la target/classes/com/example/ticketero/model/enums/

**🔍 PUNTO DE REVISIÓN 1.2:**

✅ PASO 1.2 COMPLETADO

Archivos creados:  
\- src/main/java/com/example/ticketero/model/enums/QueueType.java  
\- src/main/java/com/example/ticketero/model/enums/TicketStatus.java  
\- src/main/java/com/example/ticketero/model/enums/AdvisorStatus.java  
\- src/main/java/com/example/ticketero/model/enums/MessageTemplate.java

Validaciones realizadas:  
\- ✅ mvn clean compile ejecutado sin errores  
\- ✅ 4 enums compiladas correctamente  
\- ✅ Métodos útiles implementados (getPrefix, isActive, etc.)  
\- ✅ Pattern matching usado en getPrefix()

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿Los enums tienen todos los valores correctos?  
2\. ¿Los tiempos promedio y prioridades son correctos?  
3\. ¿Los métodos útiles son apropiados?  
4\. ¿Se usa Java 21 pattern matching correctamente?  
5\. ¿Puedo continuar con FASE 2?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR A FASE 2...

---

## **CONTINUACIÓN DE IMPLEMENTACIÓN**

**IMPORTANTE:** El prompt continuará con:

* **FASE 2:** Entities (Ticket, Mensaje, Advisor) \- 3 pasos con revisiones  
* **FASE 3:** DTOs (5 DTOs) \- 2 pasos con revisiones  
* **FASE 4:** Repositories (3 repositories) \- 1 paso con revisión  
* **FASE 5:** Services (5 services) \- 5 pasos con revisiones  
* **FASE 6:** Controllers (2 controllers) \- 2 pasos con revisiones  
* **FASE 7:** Schedulers (2 schedulers) \- 2 pasos con revisiones

Cada paso DEBE seguir el mismo patrón:

1. Implementar código completo  
2. Validar compilación y ejecución  
3. DETENERSE y solicitar revisión exhaustiva  
4. Esperar confirmación antes de continuar

---

## **Formato de Cada Fase**

Cada fase siguiente seguirá esta estructura:

\#\# FASE X: \[Nombre de la Fase\]

\#\#\# PASO X.Y: \[Descripción del Paso\]

\*\*Tareas:\*\*  
\- \[Lista de tareas\]

\*\*Implementación:\*\*  
\[Código completo del archivo o archivos\]

\*\*Validaciones:\*\*  
\`\`\`bash  
\[Comandos para verificar\]

**🔍 PUNTO DE REVISIÓN X.Y:** \[Solicitud de revisión exhaustiva\]

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

\---

\#\# Criterios de Calidad del Código

TODO el código generado DEBE cumplir:

\*\*✅ Estándares Java 21:\*\*  
\- Records para DTOs inmutables  
\- Pattern matching donde sea apropiado  
\- Text blocks para strings largos  
\- Sealed classes si es necesario

\*\*✅ Estándares Spring Boot:\*\*  
\- Constructor injection con \`@RequiredArgsConstructor\`  
\- \`@Transactional\` en servicios de escritura  
\- Bean Validation en DTOs  
\- Logging con \`@Slf4j\`

\*\*✅ Patrones del Proyecto:\*\*  
\- Seguir estructura del código existente en GitHub  
\- Nombres de métodos descriptivos  
\- Comentarios JavaDoc en métodos públicos  
\- Manejo de excepciones apropiado

\*\*✅ Principios 80/20:\*\*  
\- Código simple y directo  
\- Sin abstracciones innecesarias  
\- Sin patrones complejos  
\- YAGNI (You Aren't Gonna Need It)

\---

\#\# Criterios de Aceptación Final

Al completar TODAS las fases, el sistema debe:

\*\*✅ Compilación:\*\*  
\- \`mvn clean compile\` sin errores  
\- \`mvn package \-DskipTests\` genera JAR

\*\*✅ Ejecución:\*\*  
\- \`mvn spring-boot:run\` inicia la aplicación  
\- Conexión a PostgreSQL exitosa  
\- Flyway ejecuta migraciones  
\- Schedulers se registran correctamente

\*\*✅ Funcionalidad:\*\*  
\- POST /api/tickets crea ticket correctamente  
\- GET /api/admin/dashboard retorna métricas  
\- MessageScheduler procesa mensajes cada 60s  
\- QueueProcessorScheduler asigna tickets cada 5s

\*\*✅ Base de Datos:\*\*  
\- 3 tablas creadas (ticket, mensaje, advisor)  
\- 5 asesores insertados  
\- Foreign keys funcionando

\---

\#\# Entregables

Al finalizar, debes haber creado:

\*\*Código Java (42+ archivos):\*\*  
\- 1 clase principal  
\- 4 enums  
\- 3 entities  
\- 5 DTOs  
\- 3 repositories  
\- 5 services  
\- 2 controllers  
\- 2 schedulers  
\- 3 exceptions  
\- 2 configuraciones

\*\*Configuración:\*\*  
\- pom.xml  
\- application.yml  
\- docker-compose.yml  
\- Dockerfile  
\- .env

\*\*Migraciones:\*\*  
\- 3 archivos SQL de Flyway

\---

\#\# INICIO DE LA IMPLEMENTACIÓN

\*\*Instrucción Final:\*\*

Implementa el código COMPLETO siguiendo la metodología "Implementar → Validar → Confirmar → Continuar".

Después de CADA paso, DETENTE y solicita revisión exhaustiva usando el formato especificado.

NO continues con el siguiente paso hasta recibir confirmación explícita.

\*\*¿Estás listo para comenzar con FASE 0, PASO 0.1?\*\*


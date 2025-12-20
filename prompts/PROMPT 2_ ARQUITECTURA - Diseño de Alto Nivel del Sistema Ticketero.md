# **PROMPT 2: ARQUITECTURA \- Diseño de Alto Nivel del Sistema Ticketero**

## **Contexto**

Eres un Arquitecto de Software Senior con 10+ años de experiencia en sistemas empresariales. Has recibido el documento de Requerimientos Funcionales aprobado y tu tarea es diseñar la **arquitectura de alto nivel** del Sistema Ticketero.

**IMPORTANTE:** Después de completar CADA paso, debes DETENERTE y solicitar una **revisión exhaustiva** antes de continuar con el siguiente paso.

---

## **Documentos de Entrada**

**Lee estos archivos que YA están en tu proyecto:**

1. `docs/REQUERIMIENTOS-NEGOCIO.md` \- Contexto de negocio  
2. `REQUERIMIENTOS-FUNCIONALES.md` \- RF-001 a RF-008 con criterios de aceptación

---

## **Metodología de Trabajo**

### **Principio Fundamental:**

**"Diseñar → Validar → Confirmar → Continuar"**

Después de CADA paso:

1. ✅ Diseña el componente arquitectónico  
2. ✅ Valida que es renderizable/correcto  
3. ✅ Revisa alineación con requerimientos  
4. ⏸️ **DETENTE y solicita revisión exhaustiva**  
5. ✅ Espera confirmación antes de continuar

### **Formato de Solicitud de Revisión:**

✅ PASO X COMPLETADO

Componente diseñado:  
\- \[Nombre del componente\]

Validaciones realizadas:  
\- \[checklist de validaciones\]

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿El diseño es correcto y completo?  
2\. ¿Está alineado con los requerimientos?  
3\. ¿La justificación técnica es sólida?  
4\. ¿Hay algo que mejorar?  
5\. ¿Puedo continuar con el siguiente paso?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **Tu Tarea**

Crear un documento de **Arquitectura de Software** profesional implementado en 7 pasos:

**PASO 1:** Stack Tecnológico con Justificaciones  
**PASO 2:** Diagrama de Contexto C4  
**PASO 3:** Diagrama de Secuencia  
**PASO 4:** Modelo de Datos ER  
**PASO 5:** Arquitectura en Capas \+ Componentes  
**PASO 6:** Decisiones Arquitectónicas (ADRs)  
**PASO 7:** Configuración y Validación Final

1. **Decisiones de Stack Tecnológico** (con justificación)  
2. **Diagrama de Contexto C4** (PlantUML)  
3. **Diagrama de Secuencia** del flujo completo (PlantUML)  
4. **Modelo de Datos ER** (PlantUML)  
5. **Arquitectura en Capas** (descripción \+ responsabilidades)  
6. **Componentes Principales** (Controllers, Services, Repositories, Schedulers)  
7. **Decisiones Arquitectónicas** con pros/contras

---

## **PASO 1: Stack Tecnológico con Justificaciones**

**Objetivo:** Seleccionar y justificar todas las tecnologías del proyecto con análisis de alternativas.

**Tareas:**

* Seleccionar Backend Framework (Java 21 \+ Spring Boot)  
* Seleccionar Base de Datos (PostgreSQL 16\)  
* Seleccionar herramienta de Migraciones (Flyway)  
* Seleccionar método de Integración Telegram (RestTemplate)  
* Seleccionar herramienta de Containerización (Docker)  
* Seleccionar Build Tool (Maven)  
* Justificar cada selección con pros/contras vs alternativas

**Implementación:**

## **PARTE 1: Stack Tecnológico (Decisión Crítica)**

Debes seleccionar y justificar las siguientes tecnologías:

### **Backend Framework**

**Selección:** Java 21 \+ Spring Boot 3.2.11

**Justificación:**

\#\#\# ¿Por qué Java 21?  
\- Virtual Threads (Project Loom): manejo eficiente de concurrencia para schedulers  
\- Records: DTOs inmutables sin boilerplate  
\- Pattern Matching: código más limpio y expresivo  
\- LTS (Long Term Support): soporte hasta 2029  
\- Ecosistema maduro para aplicaciones empresariales

\#\#\# ¿Por qué Spring Boot 3.2.11?  
\- Spring Data JPA: reducción de 80% de código de acceso a datos  
\- Spring Scheduling: @Scheduled para procesamiento asíncrono de mensajes  
\- Bean Validation: validación declarativa con @Valid  
\- Actuator: endpoints de salud y métricas out-of-the-box  
\- Amplia adopción en sector financiero (95% de instituciones)

\#\#\# Alternativas Consideradas:  
| Tecnología      | Pros                          | Contras                           | Decisión |  
|-----------------|-------------------------------|-----------------------------------|----------|  
| Node.js \+ NestJS| Async nativo, menor footprint | Menos maduro para apps críticas   | ❌ No    |  
| Go \+ Gin        | Performance superior          | Ecosistema menos maduro para CRUD | ❌ No    |  
| .NET Core       | Excelente tooling             | Licenciamiento, menos adopción    | ❌ No    |

### **Base de Datos**

**Selección:** PostgreSQL 16

**Justificación:**

\#\#\# ¿Por qué PostgreSQL 16?  
\- ACID compliant: crítico para transacciones financieras  
\- JSONB: flexibilidad para almacenar metadata de mensajes  
\- Índices avanzados: B-tree, GiST para queries complejas  
\- Row-level locking: concurrencia para asignación de tickets  
\- Particionamiento: escalabilidad para auditoría (millones de registros)  
\- Open source: sin costos de licenciamiento

\#\#\# Alternativas Consideradas:  
| Base de Datos | Pros                    | Contras                      | Decisión |  
|---------------|-------------------------|------------------------------|----------|  
| MySQL         | Amplia adopción         | Menor soporte de JSON        | ❌ No    |  
| MongoDB       | Flexible schema         | No ACID para múltiples docs  | ❌ No    |  
| Oracle        | Features empresariales  | Costos prohibitivos          | ❌ No    |

### **Migraciones de Base de Datos**

**Selección:** Flyway

**Justificación:**

\#\#\# ¿Por qué Flyway?  
\- Versionamiento automático de esquema de BD  
\- Rollback seguro en producción  
\- Integración nativa con Spring Boot  
\- Validación de checksums para detectar cambios manuales  
\- Simplicidad: archivos SQL planos (V1\_\_..., V2\_\_...)

\#\#\# Alternativa:  
\- Liquibase: más verboso (XML/YAML), overkill para este proyecto

### **Integración con Telegram**

**Selección:** Telegram Bot HTTP API \+ RestTemplate

**Justificación:**

\#\#\# ¿Por qué Telegram Bot API?  
\- Canal de notificación preferido por el cliente  
\- API HTTP simple y bien documentada  
\- Sin costo (vs WhatsApp Business API: $0.005/mensaje)  
\- Rate limits generosos (30 msg/segundo)  
\- Soporte de HTML formatting para mensajes enriquecidos

\#\#\# ¿Por qué RestTemplate (no WebClient)?  
\- Simplicidad: API síncrona más fácil de debuggear  
\- Suficiente para 25,000 mensajes/día  
\- Menor curva de aprendizaje para el equipo  
\- WebClient (reactivo) es overkill para este volumen

### **Containerización**

**Selección:** Docker \+ Docker Compose

**Justificación:**

\#\#\# ¿Por qué Docker?  
\- Paridad dev/prod: "funciona en mi máquina" → "funciona en todas"  
\- Multi-stage builds: imagen final \<150MB  
\- Aislamiento de dependencias  
\- Estándar de la industria (90% adopción)

\#\#\# ¿Por qué Docker Compose?  
\- Orquestación simple para dev/staging  
\- Definición declarativa de servicios (API \+ PostgreSQL)  
\- Redes automáticas entre contenedores  
\- Para producción en AWS: migración fácil a ECS/Fargate

### **Build Tool**

**Selección:** Maven 3.9+

**Justificación:**

\#\#\# ¿Por qué Maven?  
\- Convención sobre configuración  
\- Repositorio central con 10M+ artifacts  
\- Plugins maduros (Spring Boot Maven Plugin)  
\- Estándar en empresas financieras

---

**Validaciones:**

* \[ \] 6 tecnologías seleccionadas y justificadas  
* \[ \] Cada selección tiene tabla de alternativas con pros/contras  
* \[ \] Justificaciones son técnicamente sólidas  
* \[ \] Se consideró el contexto empresarial  
* \[ \] Todas las decisiones están alineadas con RF

**🔍 PUNTO DE REVISIÓN 1:**

✅ PASO 1 COMPLETADO

Componente diseñado:  
\- Stack Tecnológico Completo

Tecnologías seleccionadas:  
\- Backend: Java 21 \+ Spring Boot 3.2.11  
\- Base de Datos: PostgreSQL 16  
\- Migraciones: Flyway  
\- Integración: Telegram Bot API \+ RestTemplate  
\- Containerización: Docker \+ Docker Compose  
\- Build: Maven 3.9+

Validaciones realizadas:  
\- ✅ 6 tecnologías justificadas con pros/contras  
\- ✅ Tablas de alternativas incluidas  
\- ✅ Decisiones alineadas con volumen esperado (25K tickets/día)  
\- ✅ Principio 80/20 aplicado (simplicidad)

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿Las justificaciones técnicas son sólidas?  
2\. ¿Las tablas de alternativas son completas?  
3\. ¿Las decisiones son apropiadas para el contexto?  
4\. ¿Falta alguna tecnología crítica?  
5\. ¿Puedo continuar con PASO 2 (Diagrama C4)?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **PASO 2: Diagrama de Contexto C4**

**Objetivo:** Crear diagrama C4 Level 1 mostrando el sistema en su contexto con actores y sistemas externos.

**Tareas:**

* Crear diagrama PlantUML del contexto  
* Identificar actores (Cliente, Supervisor)  
* Identificar sistemas externos (Telegram, Terminal)  
* Documentar relaciones e integraciones  
* Guardar en archivo separado Y en documento

**Implementación:**

## **PARTE 2: Diagramas de Arquitectura**

### **Diagrama 1: Contexto C4 (System Context)**

**Instrucciones:**

1. Crea el archivo `docs/diagrams/01-context-diagram.puml` con este contenido  
2. **IMPORTANTE:** Incluye este mismo diagrama en el documento de arquitectura dentro de la sección "3.1 Diagrama de Contexto C4"

**Contenido del diagrama:**

@startuml Diagrama de Contexto \- Sistema Ticketero  
\!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4\_Context.puml

title Diagrama de Contexto (C4 Level 1\) \- Sistema Ticketero

' Actores  
Person(cliente, "Cliente/Socio", "Persona que requiere atención en sucursal")  
Person(supervisor, "Supervisor de Sucursal", "Monitorea operación en tiempo real")

' Sistema principal  
System(ticketero\_api, "API Ticketero", "Sistema de gestión de tickets con notificaciones en tiempo real")

' Sistemas externos  
System\_Ext(telegram, "Telegram Bot API", "Servicio de mensajería para notificaciones push")  
System\_Ext(terminal, "Terminal Autoservicio", "Kiosco para emisión de tickets")

' Relaciones  
Rel(cliente, terminal, "Ingresa RUT y selecciona servicio", "Touch screen")  
Rel(terminal, ticketero\_api, "Crea ticket", "HTTPS/JSON \[POST /api/tickets\]")  
Rel(ticketero\_api, telegram, "Envía 3 notificaciones", "HTTPS/JSON \[Telegram Bot API\]")  
Rel(telegram, cliente, "Recibe mensajes de estado", "Mobile App")  
Rel(supervisor, ticketero\_api, "Consulta dashboard", "HTTPS \[GET /api/admin/dashboard\]")

SHOW\_LEGEND()

@enduml

**En el documento ARQUITECTURA.md, incluye:**

\#\#\# 3.1 Diagrama de Contexto C4

El siguiente diagrama muestra el sistema Ticketero en su contexto, incluyendo actores externos y sistemas con los que interactúa.

\`\`\`plantuml  
@startuml Diagrama de Contexto \- Sistema Ticketero  
\!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4\_Context.puml

title Diagrama de Contexto (C4 Level 1\) \- Sistema Ticketero

' Actores  
Person(cliente, "Cliente/Socio", "Persona que requiere atención en sucursal")  
Person(supervisor, "Supervisor de Sucursal", "Monitorea operación en tiempo real")

' Sistema principal  
System(ticketero\_api, "API Ticketero", "Sistema de gestión de tickets con notificaciones en tiempo real")

' Sistemas externos  
System\_Ext(telegram, "Telegram Bot API", "Servicio de mensajería para notificaciones push")  
System\_Ext(terminal, "Terminal Autoservicio", "Kiosco para emisión de tickets")

' Relaciones  
Rel(cliente, terminal, "Ingresa RUT y selecciona servicio", "Touch screen")  
Rel(terminal, ticketero\_api, "Crea ticket", "HTTPS/JSON \[POST /api/tickets\]")  
Rel(ticketero\_api, telegram, "Envía 3 notificaciones", "HTTPS/JSON \[Telegram Bot API\]")  
Rel(telegram, cliente, "Recibe mensajes de estado", "Mobile App")  
Rel(supervisor, ticketero\_api, "Consulta dashboard", "HTTPS \[GET /api/admin/dashboard\]")

SHOW\_LEGEND()

@enduml

**Nota:** Para visualizar el diagrama, puedes usar plugins de PlantUML en tu IDE o la herramienta online http://www.plantuml.com/plantuml/

**Archivo fuente:** `docs/diagrams/01-context-diagram.puml`

\---

\*\*Validaciones:\*\*

\`\`\`bash  
\# 1\. Verificar que el diagrama es válido PlantUML  
\# Copiar el código PlantUML y pegarlo en: http://www.plantuml.com/plantuml/

\# 2\. Verificar elementos del diagrama:  
\# \- 2 Actores (Person): Cliente, Supervisor  
\# \- 1 Sistema Principal: Ticketero API  
\# \- 2 Sistemas Externos: Telegram, Terminal  
\# \- 5 Relaciones (Rel) documentadas

\# 3\. Archivo creado: docs/diagrams/01-context-diagram.puml

**Checklist:**

* \[ \] Diagrama PlantUML válido y renderizable  
* \[ \] 2 actores identificados (Cliente, Supervisor)  
* \[ \] 1 sistema principal (Ticketero API)  
* \[ \] 2 sistemas externos (Telegram, Terminal)  
* \[ \] 5 relaciones documentadas con protocolos  
* \[ \] Diagrama incluido en archivo separado Y en documento  
* \[ \] Leyenda incluida (SHOW\_LEGEND())

**🔍 PUNTO DE REVISIÓN 2:**

✅ PASO 2 COMPLETADO

Componente diseñado:  
\- Diagrama de Contexto C4 (Level 1\)

Elementos incluidos:  
\- Actores: Cliente, Supervisor  
\- Sistema: Ticketero API  
\- Externos: Telegram Bot API, Terminal Autoservicio  
\- Relaciones: 5 flujos documentados

Validaciones realizadas:  
\- ✅ Diagrama PlantUML renderiza correctamente  
\- ✅ Archivo creado: docs/diagrams/01-context-diagram.puml  
\- ✅ Diagrama embebido en ARQUITECTURA.md  
\- ✅ Protocolos especificados (HTTPS/JSON)  
\- ✅ SHOW\_LEGEND() incluida

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿El diagrama renderiza correctamente en PlantUML?  
2\. ¿Todos los actores y sistemas están identificados?  
3\. ¿Las relaciones son claras y completas?  
4\. ¿Los protocolos están especificados?  
5\. ¿Puedo continuar con PASO 3 (Diagrama de Secuencia)?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **PASO 3: Diagrama de Secuencia del Flujo Completo**

**Objetivo:** Crear diagrama de secuencia mostrando el flujo end-to-end del sistema.

**Tareas:**

* Crear diagrama PlantUML de secuencia  
* Documentar 5 fases del flujo (Creación → Mensaje 1 → Progreso → Asignación → Completar)  
* Incluir todos los componentes (Controller, Service, DB, Scheduler, etc.)  
* Guardar en archivo separado Y en documento

**Implementación:**

### **Diagrama 2: Secuencia del Flujo Completo**

**Instrucciones:**

1. Crea el archivo `docs/diagrams/02-sequence-diagram.puml` con este contenido  
2. **IMPORTANTE:** Incluye este mismo diagrama en el documento de arquitectura dentro de la sección "3.2 Diagrama de Secuencia"

**Contenido del diagrama:**

@startuml Secuencia Completa \- Sistema Ticketero

title Flujo End-to-End: Creación de Ticket y Asignación a Asesor

actor Cliente  
participant "Terminal" as Terminal  
participant "TicketController" as Controller  
participant "TicketService" as Service  
participant "TelegramService" as Telegram  
participant "PostgreSQL" as DB  
participant "MessageScheduler" as Scheduler  
participant "QueueProcessor" as QueueProc  
participant "Advisor" as Asesor

\== Fase 1: Creación de Ticket \==

Cliente \-\> Terminal: Ingresa RUT, teléfono, selecciona PERSONAL\_BANKER  
Terminal \-\> Controller: POST /api/tickets  
Controller \-\> Service: crearTicket(request)  
Service \-\> DB: INSERT INTO ticket (numero: P01, status: EN\_ESPERA, positionInQueue: 5\)  
Service \-\> DB: INSERT INTO mensaje (3 mensajes programados)  
Service \--\> Controller: TicketResponse(numero: P01, positionInQueue: 5, estimatedWait: 75min)  
Controller \--\> Terminal: HTTP 201 \+ JSON  
Terminal \--\> Cliente: Muestra ticket P01

\== Fase 2: Envío de Mensaje 1 (Inmediato) \==

note over Scheduler: Ejecuta cada 60 segundos  
Scheduler \-\> DB: SELECT mensajes WHERE estadoEnvio=PENDIENTE AND fechaProgramada \<= NOW  
DB \--\> Scheduler: \[Mensaje 1: totem\_ticket\_creado\]  
Scheduler \-\> Telegram: POST sendMessage (chatId, "✅ Ticket P01, posición \#5, 75min")  
Telegram \--\> Scheduler: {ok: true, message\_id: 123}  
Scheduler \-\> DB: UPDATE mensaje SET estadoEnvio=ENVIADO, telegramMessageId=123  
Scheduler \--\> Cliente: Notificación en Telegram

\== Fase 3: Progreso de Cola (cuando posición \<= 3\) \==

note over QueueProc: Ejecuta cada 5 segundos  
QueueProc \-\> DB: Recalcula posiciones de todos los tickets EN\_ESPERA  
QueueProc \-\> DB: UPDATE ticket SET positionInQueue \= (nueva posición)  
QueueProc \-\> DB: SELECT tickets WHERE positionInQueue \<= 3 AND status \= EN\_ESPERA  
DB \--\> QueueProc: \[Ticket P01, posición: 3\]  
QueueProc \-\> DB: UPDATE ticket SET status \= PROXIMO

note over Scheduler: Detecta Mensaje 2 programado  
Scheduler \-\> Telegram: POST sendMessage ("⏰ Pronto será tu turno P01")  
Telegram \--\> Scheduler: {ok: true}  
Scheduler \--\> Cliente: Notificación Pre-aviso

\== Fase 4: Asignación Automática a Asesor \==

QueueProc \-\> DB: SELECT advisors WHERE status=AVAILABLE ORDER BY assignedTicketsCount LIMIT 1  
DB \--\> QueueProc: \[Advisor: María González, moduleNumber: 3\]  
QueueProc \-\> DB: UPDATE ticket SET assignedAdvisor=María, assignedModuleNumber=3, status=ATENDIENDO  
QueueProc \-\> DB: UPDATE advisor SET status=BUSY, assignedTicketsCount=assignedTicketsCount+1

note over Scheduler: Detecta Mensaje 3 programado  
Scheduler \-\> Telegram: POST sendMessage ("🔔 ES TU TURNO P01\! Módulo 3, Asesora: María González")  
Telegram \--\> Scheduler: {ok: true}  
Scheduler \--\> Cliente: Notificación Turno Activo

QueueProc \-\> Asesor: Notifica en terminal del asesor  
Asesor \--\> Cliente: Atiende al cliente en módulo 3

\== Fase 5: Completar Atención \==

Asesor \-\> Controller: PUT /api/admin/advisors/1/complete-ticket  
Controller \-\> Service: completarTicket(ticketId)  
Service \-\> DB: UPDATE ticket SET status=COMPLETADO  
Service \-\> DB: UPDATE advisor SET status=AVAILABLE, assignedTicketsCount=assignedTicketsCount-1  
Service \-\> DB: INSERT INTO auditoria (evento: TICKET\_COMPLETADO)  
Service \--\> Controller: {success: true}

@enduml

**En el documento ARQUITECTURA.md, incluye:**

\#\#\# 3.2 Diagrama de Secuencia

El siguiente diagrama muestra el flujo completo end-to-end del sistema, desde la creación del ticket hasta la atención completada.

\`\`\`plantuml  
@startuml Secuencia Completa \- Sistema Ticketero

title Flujo End-to-End: Creación de Ticket y Asignación a Asesor

actor Cliente  
participant "Terminal" as Terminal  
participant "TicketController" as Controller  
participant "TicketService" as Service  
participant "TelegramService" as Telegram  
participant "PostgreSQL" as DB  
participant "MessageScheduler" as Scheduler  
participant "QueueProcessor" as QueueProc  
participant "Advisor" as Asesor

\== Fase 1: Creación de Ticket \==

Cliente \-\> Terminal: Ingresa RUT, teléfono, selecciona PERSONAL\_BANKER  
Terminal \-\> Controller: POST /api/tickets  
Controller \-\> Service: crearTicket(request)  
Service \-\> DB: INSERT INTO ticket (numero: P01, status: EN\_ESPERA, positionInQueue: 5\)  
Service \-\> DB: INSERT INTO mensaje (3 mensajes programados)  
Service \--\> Controller: TicketResponse(numero: P01, positionInQueue: 5, estimatedWait: 75min)  
Controller \--\> Terminal: HTTP 201 \+ JSON  
Terminal \--\> Cliente: Muestra ticket P01

\== Fase 2: Envío de Mensaje 1 (Inmediato) \==

note over Scheduler: Ejecuta cada 60 segundos  
Scheduler \-\> DB: SELECT mensajes WHERE estadoEnvio=PENDIENTE AND fechaProgramada \<= NOW  
DB \--\> Scheduler: \[Mensaje 1: totem\_ticket\_creado\]  
Scheduler \-\> Telegram: POST sendMessage (chatId, "✅ Ticket P01, posición \#5, 75min")  
Telegram \--\> Scheduler: {ok: true, message\_id: 123}  
Scheduler \-\> DB: UPDATE mensaje SET estadoEnvio=ENVIADO, telegramMessageId=123  
Scheduler \--\> Cliente: Notificación en Telegram

\== Fase 3: Progreso de Cola (cuando posición \<= 3\) \==

note over QueueProc: Ejecuta cada 5 segundos  
QueueProc \-\> DB: Recalcula posiciones de todos los tickets EN\_ESPERA  
QueueProc \-\> DB: UPDATE ticket SET positionInQueue \= (nueva posición)  
QueueProc \-\> DB: SELECT tickets WHERE positionInQueue \<= 3 AND status \= EN\_ESPERA  
DB \--\> QueueProc: \[Ticket P01, posición: 3\]  
QueueProc \-\> DB: UPDATE ticket SET status \= PROXIMO

note over Scheduler: Detecta Mensaje 2 programado  
Scheduler \-\> Telegram: POST sendMessage ("⏰ Pronto será tu turno P01")  
Telegram \--\> Scheduler: {ok: true}  
Scheduler \--\> Cliente: Notificación Pre-aviso

\== Fase 4: Asignación Automática a Asesor \==

QueueProc \-\> DB: SELECT advisors WHERE status=AVAILABLE ORDER BY assignedTicketsCount LIMIT 1  
DB \--\> QueueProc: \[Advisor: María González, moduleNumber: 3\]  
QueueProc \-\> DB: UPDATE ticket SET assignedAdvisor=María, assignedModuleNumber=3, status=ATENDIENDO  
QueueProc \-\> DB: UPDATE advisor SET status=BUSY, assignedTicketsCount=assignedTicketsCount+1

note over Scheduler: Detecta Mensaje 3 programado  
Scheduler \-\> Telegram: POST sendMessage ("🔔 ES TU TURNO P01\! Módulo 3, Asesora: María González")  
Telegram \--\> Scheduler: {ok: true}  
Scheduler \--\> Cliente: Notificación Turno Activo

QueueProc \-\> Asesor: Notifica en terminal del asesor  
Asesor \--\> Cliente: Atiende al cliente en módulo 3

\== Fase 5: Completar Atención \==

Asesor \-\> Controller: PUT /api/admin/advisors/1/complete-ticket  
Controller \-\> Service: completarTicket(ticketId)  
Service \-\> DB: UPDATE ticket SET status=COMPLETADO  
Service \-\> DB: UPDATE advisor SET status=AVAILABLE, assignedTicketsCount=assignedTicketsCount-1  
Service \-\> DB: INSERT INTO auditoria (evento: TICKET\_COMPLETADO)  
Service \--\> Controller: {success: true}

@enduml

**Descripción de las Fases:**

* **Fase 1:** Cliente crea ticket en terminal, sistema calcula posición real  
* **Fase 2:** Scheduler envía confirmación inmediata vía Telegram  
* **Fase 3:** Sistema monitorea progreso, envía pre-aviso cuando posición ≤ 3  
* **Fase 4:** Asignación automática a asesor disponible, envía notificación final  
* **Fase 5:** Asesor completa atención, sistema libera recursos

**Archivo fuente:** `docs/diagrams/02-sequence-diagram.puml`

\---

\*\*Validaciones:\*\*

\`\`\`bash  
\# 1\. Verificar que el diagrama es válido PlantUML  
\# Renderizar en: http://www.plantuml.com/plantuml/

\# 2\. Verificar elementos del diagrama:  
\# \- 8 participantes identificados  
\# \- 5 fases claramente separadas (== Fase X \==)  
\# \- Notas explicativas en schedulers  
\# \- Flujo completo desde creación hasta completar

\# 3\. Archivo creado: docs/diagrams/02-sequence-diagram.puml

**Checklist:**

* \[ \] Diagrama PlantUML válido y renderizable  
* \[ \] 8 participantes documentados  
* \[ \] 5 fases claramente separadas  
* \[ \] Notas explicativas en schedulers  
* \[ \] Flujo completo end-to-end  
* \[ \] Diagrama incluido en archivo separado Y en documento  
* \[ \] Descripción de fases incluida

**🔍 PUNTO DE REVISIÓN 3:**

✅ PASO 3 COMPLETADO

Componente diseñado:  
\- Diagrama de Secuencia End-to-End

Elementos incluidos:  
\- Participantes: Cliente, Terminal, Controller, Service, Telegram, DB, Schedulers, Asesor  
\- Fases: 5 fases documentadas (Creación, Mensaje 1, Progreso, Asignación, Completar)  
\- Interacciones: \~30 mensajes entre componentes

Validaciones realizadas:  
\- ✅ Diagrama PlantUML renderiza correctamente  
\- ✅ Archivo creado: docs/diagrams/02-sequence-diagram.puml  
\- ✅ Diagrama embebido en ARQUITECTURA.md  
\- ✅ 5 fases claramente separadas con notas  
\- ✅ Descripción de fases incluida

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿El diagrama renderiza correctamente?  
2\. ¿Las 5 fases son claras y completas?  
3\. ¿Todos los componentes están incluidos?  
4\. ¿El flujo es lógico y secuencial?  
5\. ¿Puedo continuar con PASO 4 (Modelo de Datos ER)?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **PASO 4: Modelo de Datos ER**

**Objetivo:** Crear diagrama ER con las 3 entidades del sistema y sus relaciones.

**Tareas:**

* Crear diagrama PlantUML ER  
* Documentar 3 tablas (ticket, mensaje, advisor)  
* Incluir todos los campos con tipos  
* Documentar relaciones (1:N)  
* Incluir notas con enumeraciones  
* Guardar en archivo separado Y en documento

**Implementación:**

### **Diagrama 3: Modelo de Datos (ER Diagram)**

**Instrucciones:**

1. Crea el archivo `docs/diagrams/03-er-diagram.puml` con este contenido  
2. **IMPORTANTE:** Incluye este mismo diagrama en el documento de arquitectura dentro de la sección "3.3 Modelo de Datos ER"

**Contenido del diagrama:**

@startuml Modelo de Datos \- Sistema Ticketero

\!define Table(name,desc) class name as "desc" \<\< (T,\#FFAAAA) \>\>  
\!define primary\_key(x) \<b\>PK: x\</b\>  
\!define foreign\_key(x) \<color:red\>FK: x\</color\>  
\!define unique(x) \<color:green\>UQ: x\</color\>

hide methods  
hide stereotypes

' Entidades

Table(ticket, "ticket") {  
  primary\_key(id: BIGSERIAL)  
  unique(codigo\_referencia: UUID)  
  unique(numero: VARCHAR(10))  
  national\_id: VARCHAR(20)  
  telefono: VARCHAR(20)  
  branch\_office: VARCHAR(100)  
  queue\_type: VARCHAR(20)  
  status: VARCHAR(20)  
  position\_in\_queue: INTEGER  
  estimated\_wait\_minutes: INTEGER  
  created\_at: TIMESTAMP  
  updated\_at: TIMESTAMP  
  foreign\_key(assigned\_advisor\_id: BIGINT)  
  assigned\_module\_number: INTEGER  
}

Table(mensaje, "mensaje") {  
  primary\_key(id: BIGSERIAL)  
  foreign\_key(ticket\_id: BIGINT)  
  plantilla: VARCHAR(50)  
  estado\_envio: VARCHAR(20)  
  fecha\_programada: TIMESTAMP  
  fecha\_envio: TIMESTAMP  
  telegram\_message\_id: VARCHAR(50)  
  intentos: INTEGER  
  created\_at: TIMESTAMP  
}

Table(advisor, "advisor") {  
  primary\_key(id: BIGSERIAL)  
  name: VARCHAR(100)  
  email: VARCHAR(100)  
  status: VARCHAR(20)  
  module\_number: INTEGER  
  assigned\_tickets\_count: INTEGER  
  created\_at: TIMESTAMP  
  updated\_at: TIMESTAMP  
}

' Relaciones

ticket "1" \-- "0..\*" mensaje : "tiene mensajes programados"  
advisor "1" \-- "0..\*" ticket : "atiende tickets"

' Notas

note right of ticket  
  \*\*Estados posibles:\*\*  
  \- EN\_ESPERA  
  \- PROXIMO  
  \- ATENDIENDO  
  \- COMPLETADO  
  \- CANCELADO  
  \- NO\_ATENDIDO  
    
  \*\*Tipos de cola:\*\*  
  \- CAJA  
  \- PERSONAL\_BANKER  
  \- EMPRESAS  
  \- GERENCIA  
end note

note right of mensaje  
  \*\*Plantillas:\*\*  
  \- totem\_ticket\_creado  
  \- totem\_proximo\_turno  
  \- totem\_es\_tu\_turno  
    
  \*\*Estados:\*\*  
  \- PENDIENTE  
  \- ENVIADO  
  \- FALLIDO  
end note

note right of advisor  
  \*\*Estados:\*\*  
  \- AVAILABLE  
  \- BUSY  
  \- OFFLINE  
    
  \*\*Módulos:\*\* 1-5  
end note

@enduml

**En el documento ARQUITECTURA.md, incluye:**

\#\#\# 3.3 Modelo de Datos ER

El siguiente diagrama muestra el modelo entidad-relación de la base de datos PostgreSQL.

\`\`\`plantuml  
@startuml Modelo de Datos \- Sistema Ticketero

\!define Table(name,desc) class name as "desc" \<\< (T,\#FFAAAA) \>\>  
\!define primary\_key(x) \<b\>PK: x\</b\>  
\!define foreign\_key(x) \<color:red\>FK: x\</color\>  
\!define unique(x) \<color:green\>UQ: x\</color\>

hide methods  
hide stereotypes

' Entidades

Table(ticket, "ticket") {  
  primary\_key(id: BIGSERIAL)  
  unique(codigo\_referencia: UUID)  
  unique(numero: VARCHAR(10))  
  national\_id: VARCHAR(20)  
  telefono: VARCHAR(20)  
  branch\_office: VARCHAR(100)  
  queue\_type: VARCHAR(20)  
  status: VARCHAR(20)  
  position\_in\_queue: INTEGER  
  estimated\_wait\_minutes: INTEGER  
  created\_at: TIMESTAMP  
  updated\_at: TIMESTAMP  
  foreign\_key(assigned\_advisor\_id: BIGINT)  
  assigned\_module\_number: INTEGER  
}

Table(mensaje, "mensaje") {  
  primary\_key(id: BIGSERIAL)  
  foreign\_key(ticket\_id: BIGINT)  
  plantilla: VARCHAR(50)  
  estado\_envio: VARCHAR(20)  
  fecha\_programada: TIMESTAMP  
  fecha\_envio: TIMESTAMP  
  telegram\_message\_id: VARCHAR(50)  
  intentos: INTEGER  
  created\_at: TIMESTAMP  
}

Table(advisor, "advisor") {  
  primary\_key(id: BIGSERIAL)  
  name: VARCHAR(100)  
  email: VARCHAR(100)  
  status: VARCHAR(20)  
  module\_number: INTEGER  
  assigned\_tickets\_count: INTEGER  
  created\_at: TIMESTAMP  
  updated\_at: TIMESTAMP  
}

' Relaciones

ticket "1" \-- "0..\*" mensaje : "tiene mensajes programados"  
advisor "1" \-- "0..\*" ticket : "atiende tickets"

' Notas

note right of ticket  
  \*\*Estados posibles:\*\*  
  \- EN\_ESPERA  
  \- PROXIMO  
  \- ATENDIENDO  
  \- COMPLETADO  
  \- CANCELADO  
  \- NO\_ATENDIDO  
    
  \*\*Tipos de cola:\*\*  
  \- CAJA  
  \- PERSONAL\_BANKER  
  \- EMPRESAS  
  \- GERENCIA  
end note

note right of mensaje  
  \*\*Plantillas:\*\*  
  \- totem\_ticket\_creado  
  \- totem\_proximo\_turno  
  \- totem\_es\_tu\_turno  
    
  \*\*Estados:\*\*  
  \- PENDIENTE  
  \- ENVIADO  
  \- FALLIDO  
end note

note right of advisor  
  \*\*Estados:\*\*  
  \- AVAILABLE  
  \- BUSY  
  \- OFFLINE  
    
  \*\*Módulos:\*\* 1-5  
end note

@enduml

**Descripción de las Relaciones:**

* **ticket ← mensaje (1:N):** Un ticket puede tener múltiples mensajes programados (confirmación, pre-aviso, turno activo)  
* **advisor ← ticket (1:N):** Un asesor puede atender múltiples tickets (pero solo 1 a la vez en estado ATENDIENDO)

**Índices Importantes:**

* `ticket.codigo_referencia` (UNIQUE): Búsqueda rápida por UUID  
* `ticket.numero` (UNIQUE): Búsqueda por número de ticket  
* `ticket.national_id`: Validación de ticket activo por cliente  
* `ticket.status`: Filtrado de tickets por estado  
* `mensaje.estado_envio + fecha_programada`: Query del scheduler  
* `advisor.status`: Selección de asesores disponibles

**Archivo fuente:** `docs/diagrams/03-er-diagram.puml`

\---

\*\*Validaciones:\*\*

\`\`\`bash  
\# 1\. Verificar que el diagrama es válido PlantUML  
\# Renderizar en: http://www.plantuml.com/plantuml/

\# 2\. Verificar elementos:  
\# \- 3 tablas (ticket, mensaje, advisor)  
\# \- Campos con tipos de datos PostgreSQL  
\# \- Primary keys marcadas (PK)  
\# \- Foreign keys marcadas (FK)  
\# \- Unique constraints marcadas (UQ)  
\# \- 2 relaciones (1:N)  
\# \- Notas con enumeraciones

\# 3\. Archivo creado: docs/diagrams/03-er-diagram.puml

**Checklist:**

* \[ \] Diagrama PlantUML válido y renderizable  
* \[ \] 3 tablas documentadas (ticket, mensaje, advisor)  
* \[ \] Todos los campos con tipos de datos  
* \[ \] Primary keys, Foreign keys, Unique constraints marcadas  
* \[ \] 2 relaciones documentadas  
* \[ \] Notas con enumeraciones incluidas  
* \[ \] Diagrama incluido en archivo separado Y en documento  
* \[ \] Descripción de relaciones e índices

**🔍 PUNTO DE REVISIÓN 4:**

✅ PASO 4 COMPLETADO

Componente diseñado:  
\- Modelo de Datos ER (Entidad-Relación)

Elementos incluidos:  
\- Tablas: ticket, mensaje, advisor  
\- Campos: \~25 campos totales con tipos PostgreSQL  
\- Relaciones: ticket → mensaje (1:N), advisor → ticket (1:N)  
\- Notas: Enumeraciones de estados y tipos

Validaciones realizadas:  
\- ✅ Diagrama PlantUML renderiza correctamente  
\- ✅ Archivo creado: docs/diagrams/03-er-diagram.puml  
\- ✅ Diagrama embebido en ARQUITECTURA.md  
\- ✅ Constraints (PK, FK, UQ) marcadas  
\- ✅ Descripción de índices incluida

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:  
1\. ¿El diagrama ER renderiza correctamente?  
2\. ¿Las 3 tablas tienen todos los campos necesarios?  
3\. ¿Las relaciones son correctas (1:N)?  
4\. ¿Los tipos de datos son apropiados?  
5\. ¿Puedo continuar con PASO 5 (Arquitectura en Capas)?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

---

## **PASO 5: Arquitectura en Capas y Componentes Principales**

**Objetivo:** Documentar la arquitectura en capas del sistema y los 7 componentes principales.

**Tareas:**

* Crear diagrama ASCII de capas  
* Documentar responsabilidades por capa  
* Documentar 7 componentes (2 Controllers, 5 Services, 2 Schedulers)  
* Incluir ejemplos de código para cada componente  
* Especificar dependencias entre componentes

**Implementación:**

## **PARTE 3: Arquitectura en Capas**

Describe la arquitectura en capas del sistema:

\#\# Arquitectura en Capas

\#\#\# Diagrama de Capas

┌─────────────────────────────────────────────────────────┐ │ CAPA DE PRESENTACIÓN (Controllers) │ │ \- TicketController │ │ \- AdminController │ │ \- Recibe HTTP requests │ │ \- Valida con @Valid │ │ \- Retorna ResponseEntity\<DTO\> │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ CAPA DE NEGOCIO (Services) │ │ \- TicketService │ │ \- TelegramService │ │ \- QueueManagementService │ │ \- AdvisorService │ │ \- NotificationService │ │ \- Lógica de negocio │ │ \- Transacciones (@Transactional) │ │ \- Orquestación de operaciones │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ CAPA DE DATOS (Repositories) │ │ \- TicketRepository extends JpaRepository │ │ \- MensajeRepository │ │ \- AdvisorRepository │ │ \- Queries custom con @Query │ │ \- Spring Data JPA │ └────────────────────┬────────────────────────────────────┘ │ ▼ ┌─────────────────────────────────────────────────────────┐ │ BASE DE DATOS (PostgreSQL) │ │ \- ticket (tabla principal) │ │ \- mensaje (mensajes programados) │ │ \- advisor (asesores) │ └─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐ │ CAPA ASÍNCRONA (Schedulers) │ │ \- MessageScheduler (@Scheduled fixedRate=60s) │ │ \- QueueProcessorScheduler (@Scheduled fixedRate=5s) │ │ \- Procesamiento en background │ └─────────────────────────────────────────────────────────┘

\#\#\# Responsabilidades por Capa

\#\#\#\# 1\. Controllers (Capa de Presentación)  
\*\*Responsabilidad:\*\* Manejar HTTP requests/responses  
\*\*Prohibido:\*\* Lógica de negocio, acceso directo a DB

Ejemplo:  
\`\`\`java  
@RestController  
@RequestMapping("/api/tickets")  
public class TicketController {  
      
    @PostMapping  
    public ResponseEntity\<TicketResponse\> crearTicket(@Valid @RequestBody TicketRequest request) {  
        // 1\. Validar (automático con @Valid)  
        // 2\. Delegar a service  
        // 3\. Retornar response  
    }  
}

#### **2\. Services (Capa de Negocio)**

**Responsabilidad:** Lógica de negocio, transacciones, orquestación **Prohibido:** Lógica de presentación (HTTP codes), SQL directo

Ejemplo:

@Service  
@Transactional  
public class TicketService {  
      
    public TicketResponse crearTicket(TicketRequest request) {  
        // 1\. Validar reglas de negocio (RN-001: único ticket activo)  
        // 2\. Generar número de ticket  
        // 3\. Calcular posición (RN-010)  
        // 4\. Persistir ticket  
        // 5\. Programar 3 mensajes  
        // 6\. Registrar auditoría (RN-011)  
        // 7\. Retornar response  
    }  
}

#### **3\. Repositories (Capa de Datos)**

**Responsabilidad:** Acceso a datos, queries **Prohibido:** Lógica de negocio

Ejemplo:

@Repository  
public interface TicketRepository extends JpaRepository\<Ticket, Long\> {  
      
    @Query("SELECT t FROM Ticket t WHERE t.status \= :status ORDER BY t.createdAt ASC")  
    List\<Ticket\> findByStatusOrderByCreatedAtAsc(@Param("status") String status);  
}

#### **4\. Schedulers (Capa Asíncrona)**

**Responsabilidad:** Procesamiento en background **Prohibido:** HTTP requests directos de clientes

Ejemplo:

@Component  
public class MessageScheduler {  
      
    @Scheduled(fixedRate \= 60000\) // Cada 60 segundos  
    public void procesarMensajesPendientes() {  
        // 1\. Buscar mensajes con estado=PENDIENTE y fechaProgramada \<= NOW  
        // 2\. Enviar vía TelegramService  
        // 3\. Actualizar estado a ENVIADO/FALLIDO  
    }  
}

\---

\#\# PARTE 4: Componentes Principales

Documenta cada componente con:  
\- Responsabilidad  
\- Dependencias  
\- Métodos principales  
\- Reglas de negocio aplicadas

\[Documentar los 7 componentes como se muestra en el prompt original\]

\---

\*\*Validaciones:\*\*

\- \[ \] Diagrama ASCII de capas incluido  
\- \[ \] 5 capas documentadas (Presentación, Negocio, Datos, BD, Asíncrona)  
\- \[ \] Responsabilidades por capa claras  
\- \[ \] 9 componentes documentados (TicketController, AdminController, TicketService, TelegramService, QueueManagementService, AdvisorService, NotificationService, MessageScheduler, QueueProcessorScheduler)  
\- \[ \] Ejemplos de código para cada componente  
\- \[ \] Dependencias entre componentes especificadas

\*\*🔍 PUNTO DE REVISIÓN 5:\*\*

✅ PASO 5 COMPLETADO

Componente diseñado:

* Arquitectura en Capas \+ 9 Componentes Principales

Elementos incluidos:

* Capas: 5 capas documentadas (Presentación, Negocio, Datos, BD, Asíncrona)  
* Controllers: TicketController, AdminController  
* Services: TicketService, TelegramService, QueueManagementService, AdvisorService, NotificationService  
* Schedulers: MessageScheduler, QueueProcessorScheduler  
* Ejemplos: Código de ejemplo para cada componente

Validaciones realizadas:

* ✅ Diagrama ASCII de capas claro  
* ✅ Responsabilidades de cada capa definidas  
* ✅ 9 componentes documentados  
* ✅ Dependencias entre componentes especificadas  
* ✅ Ejemplos de código incluidos

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:

1. ¿El diagrama de capas es claro?  
2. ¿Las responsabilidades son apropiadas?  
3. ¿Los 9 componentes están completos?  
4. ¿Las dependencias son correctas?  
5. ¿Puedo continuar con PASO 6 (ADRs)?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...

\---

\#\# PASO 6: Decisiones Arquitectónicas (ADRs)

\*\*Objetivo:\*\* Documentar las 5 decisiones arquitectónicas clave con formato ADR.

\*\*Tareas:\*\*  
\- Crear ADR-001 (No Circuit Breakers)  
\- Crear ADR-002 (RestTemplate vs WebClient)  
\- Crear ADR-003 (Scheduler vs Queue)  
\- Crear ADR-004 (Flyway para Migraciones)  
\- Crear ADR-005 (Bean Validation en DTOs)  
\- Cada ADR con: Contexto, Decisión, Razones, Consecuencias, Futuro

\*\*Implementación:\*\*

\#\# PARTE 5: Decisiones Arquitectónicas Clave

\`\`\`markdown  
\*\*Responsabilidad:\*\* Exponer API REST para gestión de tickets

\*\*Endpoints:\*\*  
\- POST /api/tickets \- Crear ticket (RF-001)  
\- GET /api/tickets/{uuid} \- Obtener ticket (RF-006)  
\- GET /api/tickets/{numero}/position \- Consultar posición (RF-003)

\*\*Dependencias:\*\*  
\- TicketService

\*\*Validaciones:\*\*  
\- @Valid en TicketRequest (Bean Validation)  
\- Códigos HTTP apropiados (201 Created, 409 Conflict, 400 Bad Request)

### **Componente: AdminController**

\*\*Responsabilidad:\*\* Panel administrativo para supervisores

\*\*Endpoints:\*\*  
\- GET /api/admin/dashboard \- Dashboard completo (RF-007)  
\- GET /api/admin/queues/{type} \- Estado de cola (RF-005)  
\- GET /api/admin/advisors \- Lista de asesores (RF-007)  
\- PUT /api/admin/advisors/{id}/status \- Cambiar estado asesor

\*\*Dependencias:\*\*  
\- QueueManagementService  
\- AdvisorService  
\- TicketRepository

\*\*Actualización:\*\* Dashboard se actualiza cada 5 segundos (RNF-002)

### **Componente: TicketService**

\*\*Responsabilidad:\*\* Lógica de negocio para tickets

\*\*Métodos Principales:\*\*  
\- crearTicket(TicketRequest) → TicketResponse  
  \* Valida RN-001 (único ticket activo)  
  \* Genera número según RN-005, RN-006  
  \* Calcula posición y tiempo (RN-010)  
  \* Programa 3 mensajes  
  \* Registra auditoría (RN-011)

\- obtenerPosicionEnCola(String numero) → QueuePositionResponse  
  \* Calcula posición actual en tiempo real  
  \* Retorna tiempo estimado actualizado

\*\*Dependencias:\*\*  
\- TicketRepository  
\- MensajeRepository  
\- TelegramService (para programar mensajes)

\*\*Transacciones:\*\* @Transactional para operaciones de escritura

### **Componente: TelegramService**

\*\*Responsabilidad:\*\* Integración con Telegram Bot API

\*\*Métodos Principales:\*\*  
\- enviarMensaje(String chatId, String texto) → String messageId  
  \* POST a https://api.telegram.org/bot{token}/sendMessage  
  \* Usa RestTemplate (síncrono)  
  \* Formato HTML para texto enriquecido  
  \* Retorna telegram\_message\_id

\- obtenerTextoMensaje(String plantilla, String numeroTicket) → String  
  \* Genera texto según plantilla (totem\_ticket\_creado, etc.)  
  \* Usa emojis (✅, ⏰, 🔔)

\*\*Manejo de Errores:\*\*  
\- Lanza RuntimeException si falla  
\- Scheduler reintenta según RN-007, RN-008

### **Componente: QueueManagementService**

\*\*Responsabilidad:\*\* Gestión de colas y asignación automática

\*\*Métodos Principales:\*\*  
\- asignarSiguienteTicket() → void  
  \* Ejecutado por QueueProcessorScheduler cada 5s  
  \* Selecciona asesor AVAILABLE con menor carga (RN-004)  
  \* Prioriza colas según RN-002 (GERENCIA \> EMPRESAS \> PERSONAL\_BANKER \> CAJA)  
  \* Dentro de cola: orden FIFO (RN-003)  
  \* Actualiza estado ticket a ATENDIENDO  
  \* Actualiza estado asesor a BUSY

\- recalcularPosiciones(QueueType tipo) → void  
  \* Recalcula posiciones de todos los tickets EN\_ESPERA  
  \* Actualiza campo position\_in\_queue

\*\*Dependencias:\*\*  
\- TicketRepository  
\- AdvisorRepository  
\- NotificationService (para Mensaje 3\)

### **Componente: MessageScheduler**

\*\*Responsabilidad:\*\* Envío asíncrono de mensajes programados

\*\*Configuración:\*\*  
\- @Scheduled(fixedRate \= 60000\) // Cada 60 segundos  
\- @EnableScheduling en clase principal

\*\*Lógica:\*\*  
1\. Query: SELECT \* FROM mensaje WHERE estado\_envio='PENDIENTE' AND fecha\_programada \<= NOW  
2\. Para cada mensaje:  
   \- TelegramService.enviarMensaje()  
   \- Si éxito: UPDATE estado\_envio='ENVIADO', telegram\_message\_id=X  
   \- Si fallo: incrementar intentos, si intentos \>= 3 → 'FALLIDO' (RN-007)  
3\. Reintentos con backoff: 30s, 60s, 120s (RN-008)

\*\*Manejo de Errores:\*\*  
\- Try-catch por mensaje (un fallo no detiene el scheduler)  
\- Logging detallado para debugging

### **Componente: QueueProcessorScheduler**

\*\*Responsabilidad:\*\* Procesamiento automático de colas

\*\*Configuración:\*\*  
\- @Scheduled(fixedRate \= 5000\) // Cada 5 segundos

\*\*Lógica:\*\*  
1\. Recalcular posiciones de todos los tickets EN\_ESPERA  
2\. Identificar tickets con posición \<= 3 → UPDATE status='PROXIMO' (RN-012)  
3\. Buscar asesores AVAILABLE  
4\. Si hay asesor disponible:  
   \- QueueManagementService.asignarSiguienteTicket()  
5\. Registrar auditoría de asignaciones

---

## **PARTE 5: Decisiones Arquitectónicas Clave**

Documenta las decisiones con formato ADR (Architecture Decision Record):

### **ADR-001: No usar Circuit Breakers (Resilience4j)**

**Contexto:** Telegram Bot API es un servicio externo que podría fallar.

**Decisión:** NO implementar Circuit Breaker en esta fase.

**Razones:**

* Simplicidad 80/20: Circuit Breaker agrega complejidad innecesaria  
* Volumen bajo: 25,000 mensajes/día \= 0.3 msg/segundo (no crítico)  
* Telegram tiene 99.9% uptime  
* Reintentos simples (RN-007, RN-008) son suficientes  
* Si Telegram falla, los mensajes quedan PENDIENTES y se reintenta

**Consecuencias:**

* ✅ Código más simple y mantenible  
* ✅ Menor curva de aprendizaje  
* ❌ Sin protección contra cascading failures (aceptable para este volumen)

**Futuro:**

* Fase 2 (50+ sucursales): reevaluar Resilience4j

---

### **ADR-002: RestTemplate en lugar de WebClient**

**Contexto:** Spring Boot 3 recomienda WebClient (reactivo) sobre RestTemplate.

**Decisión:** Usar RestTemplate (blocking I/O).

**Razones:**

* Simplicidad: API síncrona más fácil de debuggear  
* Volumen bajo: 0.3 requests/segundo a Telegram  
* WebClient requiere Project Reactor (curva de aprendizaje)  
* Para este volumen, blocking I/O es suficiente

**Consecuencias:**

* ✅ Código más simple  
* ✅ Stack trace más fácil de leer  
* ❌ Menor throughput (no relevante para este caso)

**Futuro:**

* Si volumen supera 10 req/segundo → migrar a WebClient

---

### **ADR-003: Scheduler en lugar de Queue (RabbitMQ/Kafka)**

**Contexto:** Mensajes deben enviarse en tiempos específicos (inmediato, cuando posición \<=3, al asignar).

**Decisión:** Usar @Scheduled \+ tabla mensaje en PostgreSQL.

**Razones:**

* Simplicidad: no requiere infraestructura adicional (RabbitMQ/Kafka)  
* Volumen bajo: 25,000 tickets/día × 3 mensajes \= 75,000 mensajes/día \= 0.9 msg/segundo  
* @Scheduled cada 60s es suficiente para este throughput  
* PostgreSQL como "queue" es confiable (ACID)

**Consecuencias:**

* ✅ Infraestructura simple (solo PostgreSQL \+ API)  
* ✅ Sin complejidad de RabbitMQ  
* ❌ Polling cada 60s (no tiempo real extremo, aceptable)

**Futuro:**

* Fase Nacional (500,000+ mensajes/día): migrar a RabbitMQ

---

### **ADR-004: Flyway para Migraciones**

**Decisión:** Usar Flyway en lugar de Liquibase o migraciones manuales.

**Razones:**

* SQL plano (fácil de leer y mantener)  
* Versionamiento automático  
* Rollback seguro  
* Integración nativa con Spring Boot

**Consecuencias:**

* ✅ Esquema versionado y auditable  
* ✅ Despliegues reproducibles

---

### **ADR-005: Bean Validation (@Valid) en DTOs**

**Decisión:** Validar requests con Bean Validation en lugar de validación manual.

**Razones:**

* Declarativo: @NotBlank, @Pattern directamente en DTOs  
* Spring lo valida automáticamente con @Valid  
* Mensajes de error estandarizados

**Ejemplo:**

public record TicketRequest(  
    @NotBlank(message \= "RUT/ID es obligatorio") String nationalId,  
    @Pattern(regexp \= "^\\\\+56\[0-9\]{9}$") String telefono,  
    @NotNull QueueType queueType  
) {}

---

## **PARTE 6: Configuración y Deployment**

### **Variables de Entorno**

| Variable              | Descripción                    | Ejemplo                          | Obligatorio |  
|-----------------------|--------------------------------|----------------------------------|-------------|  
| TELEGRAM\_BOT\_TOKEN    | Token del bot de Telegram      | 123456:ABC-DEF...                | Sí          |  
| DATABASE\_URL          | JDBC URL de PostgreSQL         | jdbc:postgresql://db:5432/...    | Sí          |  
| DATABASE\_USERNAME     | Usuario de base de datos       | ticketero\_user                   | Sí          |  
| DATABASE\_PASSWORD     | Password de base de datos      | \*\*\*                              | Sí          |  
| SPRING\_PROFILES\_ACTIVE| Profile activo (dev/prod)      | prod                             | No          |

### **Docker Compose (Desarrollo)**

version: '3.8'

services:  
  api:  
    build: .  
    ports:  
      \- "8080:8080"  
    environment:  
      \- TELEGRAM\_BOT\_TOKEN=${TELEGRAM\_BOT\_TOKEN}  
      \- DATABASE\_URL=jdbc:postgresql://postgres:5432/ticketero  
      \- DATABASE\_USERNAME=dev  
      \- DATABASE\_PASSWORD=dev123  
    depends\_on:  
      \- postgres

  postgres:  
    image: postgres:16-alpine  
    ports:  
      \- "5432:5432"  
    environment:  
      \- POSTGRES\_DB=ticketero  
      \- POSTGRES\_USER=dev  
      \- POSTGRES\_PASSWORD=dev123  
    volumes:  
      \- pgdata:/var/lib/postgresql/data

volumes:  
  pgdata:

### **Application Properties**

spring:  
  application:  
    name: ticketero-api  
    
  datasource:  
    url: ${DATABASE\_URL}  
    username: ${DATABASE\_USERNAME}  
    password: ${DATABASE\_PASSWORD}  
    
  jpa:  
    hibernate:  
      ddl-auto: validate \# Flyway maneja el schema  
    show-sql: false  
    properties:  
      hibernate.format\_sql: true  
    
  flyway:  
    enabled: true  
    baseline-on-migrate: true

telegram:  
  bot-token: ${TELEGRAM\_BOT\_TOKEN}  
  api-url: https://api.telegram.org/bot

logging:  
  level:  
    com.example.ticketero: INFO  
    org.springframework: WARN

---

## **PARTE 7: Estructura del Documento Final**

El documento de arquitectura debe tener esta estructura:

\# Arquitectura de Software \- Sistema Ticketero Digital

\*\*Proyecto:\*\* Sistema de Gestión de Tickets con Notificaciones en Tiempo Real    
\*\*Versión:\*\* 1.0    
\*\*Fecha:\*\* Diciembre 2025    
\*\*Arquitecto:\*\* \[Nombre\]

\---

\#\# 1\. Resumen Ejecutivo  
\[Descripción de alto nivel de la arquitectura propuesta\]

\#\# 2\. Stack Tecnológico  
\[Decisiones de tecnología con justificaciones\]

\#\# 3\. Diagramas de Arquitectura  
\#\#\# 3.1 Diagrama de Contexto C4  
\[PlantUML del contexto del sistema\]

\#\#\# 3.2 Diagrama de Secuencia  
\[PlantUML del flujo end-to-end\]

\#\#\# 3.3 Modelo de Datos ER  
\[PlantUML del modelo de entidad-relación\]

\#\# 4\. Arquitectura en Capas  
\[Descripción de capas y responsabilidades\]

\#\# 5\. Componentes Principales  
\[Detalle de Controllers, Services, Repositories, Schedulers\]

\#\# 6\. Decisiones Arquitectónicas (ADRs)  
\[ADR-001 a ADR-005\]

\#\# 7\. Configuración y Deployment  
\[Variables de entorno, Docker Compose, application.yml\]

\#\# 8\. Seguridad Básica  
\[Validación de inputs, protección de datos sensibles\]

\#\# 9\. Performance y Escalabilidad  
\[Estimaciones de throughput, plan de escalamiento\]

\#\# 10\. Limitaciones Conocidas  
\[Trade-offs aceptados en esta fase\]

\#\# 11\. Roadmap Técnico  
\[Futuras mejoras: Fase 2 y Fase 3\]

\#\# 12\. Referencias  
\[Links a documentación de tecnologías\]

---

**Validaciones:**

* \[ \] Variables de entorno documentadas (4 variables)  
* \[ \] docker-compose.yml completo  
* \[ \] application.yml completo con profiles  
* \[ \] Estructura del documento definida  
* \[ \] Checklist de completitud realizado

**🔍 PUNTO DE REVISIÓN FINAL 7:**

✅ PASO 7 COMPLETADO \- DOCUMENTO DE ARQUITECTURA COMPLETO

Componente diseñado:  
\- Configuración y Deployment \+ Validación Final

Elementos incluidos:  
\- Variables de entorno: 4 variables documentadas  
\- Docker Compose: PostgreSQL \+ API configurados  
\- Application.yml: Configuración Spring Boot completa  
\- Estructura: 12 secciones definidas

Validaciones realizadas:  
\- ✅ Variables de entorno documentadas  
\- ✅ docker-compose.yml funcional  
\- ✅ application.yml con todos los settings  
\- ✅ Estructura del documento completa

CHECKLIST FINAL DE COMPLETITUD:

Contenido:  
\- ✅ Stack Tecnológico (6 tecnologías justificadas)  
\- ✅ Diagrama C4 (renderizable en PlantUML)  
\- ✅ Diagrama de Secuencia (5 fases documentadas)  
\- ✅ Modelo ER (3 tablas, 2 relaciones)  
\- ✅ Arquitectura en Capas (5 capas)  
\- ✅ 9 Componentes documentados  
\- ✅ 5 ADRs con formato estándar  
\- ✅ Configuración completa

Diagramas:  
\- ✅ 3 archivos .puml creados en docs/diagrams/  
\- ✅ 3 diagramas embebidos en documento  
\- ✅ Todos renderizables en PlantUML

Calidad:  
\- ✅ Justificaciones técnicas sólidas  
\- ✅ Decisiones alineadas con requerimientos  
\- ✅ Ejemplos de código incluidos  
\- ✅ Formato profesional y consistente

🔍 SOLICITO REVISIÓN EXHAUSTIVA FINAL:

Por favor, revisa:  
1\. ¿Todos los diagramas renderizan correctamente?  
2\. ¿Las decisiones arquitectónicas son sólidas?  
3\. ¿La configuración es completa?  
4\. ¿El documento está listo para revisión técnica?  
5\. ¿Hay algo que mejorar antes de declararlo completo?

⏸️ ESPERANDO CONFIRMACIÓN FINAL...

SI TODO ESTÁ CORRECTO, EL DOCUMENTO DE ARQUITECTURA ESTÁ COMPLETO.

---

## **Criterios de Calidad**

Tu documento DEBE cumplir:

**✅ Completitud:**

* \[ \] 3 diagramas PlantUML generados  
* \[ \] Stack tecnológico completo (6 tecnologías justificadas)  
* \[ \] 7 componentes documentados  
* \[ \] 5 ADRs con contexto/decisión/consecuencias  
* \[ \] Configuración completa (env vars, docker-compose, application.yml)

**✅ Claridad:**

* \[ \] Diagramas renderizables en PlantUML  
* \[ \] Justificaciones técnicas sólidas  
* \[ \] Pros/contras de alternativas consideradas

**✅ Profesionalismo:**

* \[ \] Formato ADR estándar  
* \[ \] Diagramas C4 level 1  
* \[ \] Responsabilidades claras por capa

**✅ Alineación con Código:**

* \[ \] Componentes mapeados a clases Java reales  
* \[ \] Endpoints mapeados a métodos de controllers  
* \[ \] Entidades mapeadas a tablas SQL

---

## **Restricciones**

**❌ NO incluir:**

* Implementación de código Java (eso es PROMPT 4\)  
* Scripts SQL de migraciones (eso es PROMPT 3\)

**✅ SÍ incluir:**

* Nombres de clases y métodos principales  
* Estructura de paquetes (com.example.ticketero.controller, etc.)  
* Decisiones técnicas justificadas

---

## **Entregable**

**Archivo:** `ARQUITECTURA.md`  
**Ubicación:** `docs/`  
**Diagramas:** `docs/diagrams/01-context-diagram.puml`, `02-sequence-diagram.puml`, `03-er-diagram.puml`  
**Longitud esperada:** 30-40 páginas (8,000-10,000 palabras)

Este documento será la entrada para:

* PROMPT 3: Plan Detallado de Implementación  
* Revisión técnica por equipo de desarrollo  
* Aprobación por arquitectos senior

---

**IMPORTANTE:** Los diagramas PlantUML deben ser **renderizables** directamente. Prueba cada diagrama en http://www.plantuml.com/plantuml/ antes de finalizar el documento.


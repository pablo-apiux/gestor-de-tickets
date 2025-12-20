# Requerimientos Funcionales - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Autor:** Analista de Negocio Senior

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requerimientos funcionales del Sistema Ticketero Digital, diseñado para modernizar la experiencia de atención en sucursales mediante:

- Digitalización completa del proceso de tickets
- Notificaciones automáticas en tiempo real vía Telegram
- Movilidad del cliente durante la espera
- Asignación inteligente de clientes a ejecutivos
- Panel de monitoreo para supervisión operacional

### 1.2 Alcance

Este documento cubre:

- ✅ 8 Requerimientos Funcionales (RF-001 a RF-008)
- ✅ 13 Reglas de Negocio (RN-001 a RN-013)
- ✅ Criterios de aceptación en formato Gherkin
- ✅ Modelo de datos funcional
- ✅ Matriz de trazabilidad

Este documento NO cubre:

- ❌ Arquitectura técnica (ver documento ARQUITECTURA.md)
- ❌ Tecnologías de implementación
- ❌ Diseño de interfaces de usuario

### 1.3 Definiciones

| Término | Definición |
|---------|------------|
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Cola | Fila virtual de tickets esperando atención |
| Asesor | Ejecutivo bancario que atiende clientes |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Chat ID | Identificador único de usuario en Telegram |
| UUID | Identificador único universal para tickets |

## 2. Reglas de Negocio

Las siguientes reglas de negocio aplican transversalmente a todos los requerimientos funcionales:

**RN-001: Unicidad de Ticket Activo**  
Un cliente solo puede tener 1 ticket activo a la vez. Los estados activos son: EN_ESPERA, PROXIMO, ATENDIENDO. Si un cliente intenta crear un nuevo ticket teniendo uno activo, el sistema debe rechazar la solicitud con error HTTP 409 Conflict.

**RN-002: Prioridad de Colas**  
Las colas tienen prioridades numéricas para asignación automática:
- GERENCIA: prioridad 4 (máxima)
- EMPRESAS: prioridad 3
- PERSONAL_BANKER: prioridad 2
- CAJA: prioridad 1 (mínima)

Cuando un asesor se libera, el sistema asigna primero tickets de colas con mayor prioridad.

**RN-003: Orden FIFO Dentro de Cola**  
Dentro de una misma cola, los tickets se procesan en orden FIFO (First In, First Out). El ticket más antiguo (createdAt menor) se asigna primero.

**RN-004: Balanceo de Carga Entre Asesores**  
Al asignar un ticket, el sistema selecciona el asesor AVAILABLE con menor valor de assignedTicketsCount, distribuyendo equitativamente la carga de trabajo.

**RN-005: Formato de Número de Ticket**  
El número de ticket sigue el formato: [Prefijo][Número secuencial 01-99]
- Prefijo: 1 letra según el tipo de cola
- Número: 2 dígitos, del 01 al 99, reseteado diariamente

Ejemplos: C01, P15, E03, G02

**RN-006: Prefijos por Tipo de Cola**  
- CAJA → C
- PERSONAL_BANKER → P
- EMPRESAS → E
- GERENCIA → G

**RN-007: Reintentos Automáticos de Mensajes**  
Si el envío de un mensaje a Telegram falla, el sistema reintenta automáticamente hasta 3 veces antes de marcarlo como FALLIDO.

**RN-008: Backoff Exponencial en Reintentos**  
Los reintentos de mensajes usan backoff exponencial:
- Intento 1: inmediato
- Intento 2: después de 30 segundos
- Intento 3: después de 60 segundos
- Intento 4: después de 120 segundos

**RN-009: Estados de Ticket**  
Un ticket puede estar en uno de estos estados:
- EN_ESPERA: esperando asignación a asesor
- PROXIMO: próximo a ser atendido (posición ≤ 3)
- ATENDIENDO: siendo atendido por un asesor
- COMPLETADO: atención finalizada exitosamente
- CANCELADO: cancelado por cliente o sistema
- NO_ATENDIDO: cliente no se presentó cuando fue llamado

**RN-010: Cálculo de Tiempo Estimado**  
El tiempo estimado de espera se calcula como:
tiempoEstimado = posiciónEnCola × tiempoPromedioCola

Donde tiempoPromedioCola varía por tipo:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos

**RN-011: Auditoría Obligatoria**  
Todos los eventos críticos del sistema deben registrarse en auditoría con: timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado.

**RN-012: Umbral de Pre-aviso**  
El sistema envía el Mensaje 2 (pre-aviso) cuando la posición del ticket es ≤ 3, indicando que el cliente debe acercarse a la sucursal.

**RN-013: Estados de Asesor**  
Un asesor puede estar en uno de estos estados:
- AVAILABLE: disponible para recibir asignaciones
- BUSY: atendiendo un cliente (no recibe nuevas asignaciones)
- OFFLINE: no disponible (almuerzo, capacitación, etc.)

## 3. Enumeraciones

### 3.1 QueueType

Tipos de cola disponibles en el sistema:

| Valor | Display Name | Tiempo Promedio | Prioridad | Prefijo |
|-------|--------------|-----------------|-----------|---------|
| CAJA | Caja | 5 min | 1 | C |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 | P |
| EMPRESAS | Empresas | 20 min | 3 | E |
| GERENCIA | Gerencia | 30 min | 4 | G |

### 3.2 TicketStatus

Estados posibles de un ticket:

| Valor | Descripción | ¿Es Activo? |
|-------|-------------|------------|
| EN_ESPERA | Esperando asignación | Sí |
| PROXIMO | Próximo a ser atendido | Sí |
| ATENDIENDO | Siendo atendido | Sí |
| COMPLETADO | Atención finalizada | No |
| CANCELADO | Cancelado | No |
| NO_ATENDIDO | Cliente no se presentó | No |

### 3.3 AdvisorStatus

Estados posibles de un asesor:

| Valor | Descripción | ¿Recibe Asignaciones? |
|-------|-------------|----------------------|
| AVAILABLE | Disponible | Sí |
| BUSY | Atendiendo cliente | No |
| OFFLINE | No disponible | No |

### 3.4 MessageTemplate

Plantillas de mensajes para Telegram:

| Valor | Descripción | Momento de Envío |
|-------|-------------|------------------|
| totem_ticket_creado | Confirmación de creación | Inmediato al crear ticket |
| totem_proximo_turno | Pre-aviso | Cuando posición ≤ 3 |
| totem_es_tu_turno | Turno activo | Al asignar a asesor |

## 4. Requerimientos Funcionales

### RF-001: Crear Ticket Digital

**Descripción:** El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Terminal de autoservicio disponible y funcional
- Sistema de gestión de colas operativo
- Conexión a base de datos activa

**Modelo de Datos (Campos del Ticket):**
- codigoReferencia: UUID único (ej: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
- numero: String formato específico por cola (ej: "C01", "P15", "E03", "G02")
- nationalId: String, identificación nacional del cliente
- telefono: String, número de teléfono para Telegram
- branchOffice: String, nombre de la sucursal
- queueType: Enum (CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA)
- status: Enum (EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO)
- positionInQueue: Integer, posición actual en cola (calculada en tiempo real)
- estimatedWaitMinutes: Integer, minutos estimados de espera
- createdAt: Timestamp, fecha/hora de creación
- assignedAdvisor: Relación a entidad Advisor (null inicialmente)
- assignedModuleNumber: Integer 1-5 (null inicialmente)

**Reglas de Negocio Aplicables:**
- RN-001: Un cliente solo puede tener 1 ticket activo a la vez
- RN-005: Número de ticket formato: [Prefijo][Número secuencial 01-99]
- RN-006: Prefijos por cola: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia
- RN-010: Cálculo de tiempo estimado: posiciónEnCola × tiempoPromedioCola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Creación exitosa de ticket para cola de Caja**
```gherkin
Given el cliente con nationalId "12345678-9" no tiene tickets activos
And el terminal está en pantalla de selección de servicio
When el cliente ingresa:
  | Campo        | Valor           |
  | nationalId   | 12345678-9      |
  | telefono     | +56912345678    |
  | branchOffice | Sucursal Centro |
  | queueType    | CAJA            |
Then el sistema genera un ticket con:
  | Campo                 | Valor Esperado                    |
  | codigoReferencia      | UUID válido                       |
  | numero                | "C[01-99]"                        |
  | status                | EN_ESPERA                         |
  | positionInQueue       | Número > 0                        |
  | estimatedWaitMinutes  | positionInQueue × 5               |
  | assignedAdvisor       | null                              |
  | assignedModuleNumber  | null                              |
And el sistema almacena el ticket en base de datos
And el sistema programa 3 mensajes de Telegram
And el sistema retorna HTTP 201 con JSON:
  {
    "identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "numero": "C01",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 25,
    "queueType": "CAJA"
  }
```

**Escenario 2: Error - Cliente ya tiene ticket activo**
```gherkin
Given el cliente con nationalId "12345678-9" tiene un ticket activo:
  | numero | status     | queueType      |
  | P05    | EN_ESPERA  | PERSONAL_BANKER|
When el cliente intenta crear un nuevo ticket con queueType CAJA
Then el sistema rechaza la creación
And el sistema retorna HTTP 409 Conflict con JSON:
  {
    "error": "TICKET_ACTIVO_EXISTENTE",
    "mensaje": "Ya tienes un ticket activo: P05",
    "ticketActivo": {
      "numero": "P05",
      "positionInQueue": 3,
      "estimatedWaitMinutes": 45
    }
  }
And el sistema NO crea un nuevo ticket
```

**Escenario 3: Validación - RUT/ID inválido**
```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa nationalId vacío
Then el sistema retorna HTTP 400 Bad Request con JSON:
  {
    "error": "VALIDACION_FALLIDA",
    "campos": {
      "nationalId": "El RUT/ID es obligatorio"
    }
  }
And el sistema NO crea el ticket
```

**Escenario 4: Validación - Teléfono en formato inválido**
```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa telefono "123"
Then el sistema retorna HTTP 400 Bad Request
And el mensaje de error especifica formato requerido "+56XXXXXXXXX"
```

**Escenario 5: Cálculo de posición - Primera persona en cola**
```gherkin
Given la cola de tipo PERSONAL_BANKER está vacía
When el cliente crea un ticket para PERSONAL_BANKER
Then el sistema calcula positionInQueue = 1
And estimatedWaitMinutes = 15
And el número de ticket es "P01"
```

**Escenario 6: Cálculo de posición - Cola con tickets existentes**
```gherkin
Given la cola de tipo EMPRESAS tiene 4 tickets EN_ESPERA
When el cliente crea un nuevo ticket para EMPRESAS
Then el sistema calcula positionInQueue = 5
And estimatedWaitMinutes = 100
And el cálculo es: 5 × 20min = 100min
```

**Escenario 7: Creación sin teléfono (cliente no quiere notificaciones)**
```gherkin
Given el cliente no proporciona número de teléfono
When el cliente crea un ticket
Then el sistema crea el ticket exitosamente
And el sistema NO programa mensajes de Telegram
```

**Postcondiciones:**
- Ticket almacenado en base de datos con estado EN_ESPERA
- 3 mensajes programados (si hay teléfono)
- Evento de auditoría registrado: "TICKET_CREADO"

**Endpoints HTTP:**
- `POST /api/tickets` - Crear nuevo ticket

---

### RF-002: Enviar Notificaciones Automáticas vía Telegram

**Descripción:** El sistema debe enviar automáticamente tres tipos de mensajes vía Telegram a los clientes que proporcionaron su número de teléfono al crear el ticket. Los mensajes se envían en momentos específicos del proceso: confirmación inmediata, pre-aviso cuando quedan 3 personas adelante, y notificación de turno activo al ser asignado a un asesor. El sistema debe manejar reintentos automáticos en caso de fallos de envío.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket creado con teléfono válido
- Telegram Bot configurado y activo
- Cliente tiene cuenta de Telegram
- Conexión a Telegram API disponible

**Modelo de Datos (Entidad Mensaje):**
- id: BIGSERIAL (primary key)
- ticket_id: BIGINT (foreign key a ticket)
- plantilla: String (totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno)
- estadoEnvio: Enum (PENDIENTE, ENVIADO, FALLIDO)
- fechaProgramada: Timestamp
- fechaEnvio: Timestamp (nullable)
- telegramMessageId: String (nullable, retornado por Telegram API)
- intentos: Integer (contador de reintentos, default 0)

**Plantillas de Mensajes:**

**1. totem_ticket_creado:**
```
✅ <b>Ticket Creado</b>

Tu número de turno: <b>{numero}</b>
Posición en cola: <b>#{posicion}</b>
Tiempo estimado: <b>{tiempo} minutos</b>

Te notificaremos cuando estés próximo.
```

**2. totem_proximo_turno:**
```
⏰ <b>¡Pronto será tu turno!</b>

Turno: <b>{numero}</b>
Faltan aproximadamente 3 turnos.

Por favor, acércate a la sucursal.
```

**3. totem_es_tu_turno:**
```
🔔 <b>¡ES TU TURNO {numero}!</b>

Dirígete al módulo: <b>{modulo}</b>
Asesor: <b>{nombreAsesor}</b>
```

**Reglas de Negocio Aplicables:**
- RN-007: 3 reintentos automáticos para mensajes fallidos
- RN-008: Backoff exponencial (30s, 60s, 120s)
- RN-011: Auditoría obligatoria de envíos
- RN-012: Mensaje 2 cuando posición ≤ 3

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Envío exitoso del Mensaje 1 (confirmación)**
```gherkin
Given un ticket "C05" fue creado con telefono "+56912345678"
And el sistema programó 3 mensajes para el ticket
When el scheduler procesa el mensaje "totem_ticket_creado"
Then el sistema envía mensaje a Telegram API con:
  | Campo    | Valor                                    |
  | chat_id  | +56912345678                             |
  | text     | "✅ <b>Ticket Creado</b>..."                |
  | parse_mode | HTML                                   |
And Telegram API retorna HTTP 200 con message_id "12345"
And el sistema actualiza el mensaje con:
  | Campo              | Valor                    |
  | estadoEnvio        | ENVIADO                  |
  | fechaEnvio         | timestamp actual         |
  | telegramMessageId  | "12345"                  |
  | intentos           | 1                        |
And el sistema registra evento de auditoría "MENSAJE_ENVIADO"
```

**Escenario 2: Envío exitoso del Mensaje 2 (pre-aviso)**
```gherkin
Given un ticket "P03" tiene positionInQueue = 3
And el ticket tiene telefono "+56987654321"
When el sistema detecta que posición ≤ 3
Then el sistema envía mensaje "totem_proximo_turno" con texto:
  "⏰ <b>¡Pronto será tu turno!</b>
   Turno: <b>P03</b>
   Faltan aproximadamente 3 turnos.
   Por favor, acércate a la sucursal."
And el mensaje se marca como ENVIADO
```

**Escenario 3: Envío exitoso del Mensaje 3 (turno activo)**
```gherkin
Given un ticket "E02" fue asignado al asesor "Juan Pérez" en módulo 3
And el ticket tiene telefono "+56911223344"
When el sistema procesa la asignación
Then el sistema envía mensaje "totem_es_tu_turno" con texto:
  "🔔 <b>¡ES TU TURNO E02!</b>
   Dirígete al módulo: <b>3</b>
   Asesor: <b>Juan Pérez</b>"
And el mensaje se marca como ENVIADO
```

**Escenario 4: Fallo de red en primer intento, éxito en segundo**
```gherkin
Given un mensaje "totem_ticket_creado" está PENDIENTE
When el sistema intenta enviar el mensaje
And Telegram API retorna HTTP 500 (error de servidor)
Then el sistema marca el mensaje con:
  | Campo       | Valor     |
  | estadoEnvio | PENDIENTE |
  | intentos    | 1         |
And el sistema programa reintento en 30 segundos
When el sistema reintenta después de 30 segundos
And Telegram API retorna HTTP 200 con message_id "67890"
Then el sistema marca el mensaje como ENVIADO
And intentos = 2
```

**Escenario 5: 3 reintentos fallidos → estado FALLIDO**
```gherkin
Given un mensaje "totem_proximo_turno" ha fallado 3 veces
And el mensaje tiene intentos = 3
When el sistema intenta el 4to envío
And Telegram API retorna HTTP 400 (número inválido)
Then el sistema marca el mensaje con:
  | Campo       | Valor   |
  | estadoEnvio | FALLIDO |
  | intentos    | 4       |
And el sistema NO programa más reintentos
And el sistema registra evento de auditoría "MENSAJE_FALLIDO"
```

**Escenario 6: Backoff exponencial entre reintentos**
```gherkin
Given un mensaje falló en el primer intento
When el sistema programa el primer reintento
Then el reintento se programa en 30 segundos
Given el mensaje falló en el segundo intento
When el sistema programa el segundo reintento
Then el reintento se programa en 60 segundos
Given el mensaje falló en el tercer intento
When el sistema programa el tercer reintento
Then el reintento se programa en 120 segundos
```

**Escenario 7: Cliente sin teléfono - No se programan mensajes**
```gherkin
Given un ticket "C03" fue creado sin teléfono
And el campo telefono es null o vacío
When el sistema procesa la creación del ticket
Then el sistema NO crea registros en tabla Mensaje
And el sistema NO programa mensajes de Telegram
And el ticket se crea exitosamente sin notificaciones
When el ticket cambia a posición ≤ 3
Then el sistema NO envía mensaje "totem_proximo_turno"
When el ticket es asignado a un asesor
Then el sistema NO envía mensaje "totem_es_tu_turno"
```

**Postcondiciones:**
- Mensaje insertado en BD con estado según resultado
- telegram_message_id almacenado si éxito
- Intentos incrementado en cada reintento
- Evento de auditoría registrado

**Endpoints HTTP:**
- Ninguno (proceso interno automatizado por scheduler)

---

### RF-003: Calcular Posición y Tiempo Estimado

**Descripción:** El sistema debe calcular en tiempo real la posición exacta del cliente en cola y estimar el tiempo de espera basado en la posición actual, tiempo promedio de atención por tipo de cola, y cantidad de asesores disponibles. El cálculo debe actualizarse automáticamente cuando otros tickets cambian de estado o se asignan a asesores.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket existe en base de datos
- Sistema de colas operativo
- Datos de tiempos promedio configurados

**Algoritmos de Cálculo:**

**Posición en Cola:**
```
posición = COUNT(tickets EN_ESPERA de la misma cola creados antes de este ticket) + 1
```

**Tiempo Estimado:**
```
tiempoEstimado = posiciónEnCola × tiempoPromedioCola
```

**Tiempos Promedio por Cola:**
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos

**Reglas de Negocio Aplicables:**
- RN-003: Orden FIFO dentro de cola (createdAt menor = mayor prioridad)
- RN-010: Fórmula de cálculo de tiempo estimado
- RN-012: Cambio a estado PROXIMO cuando posición ≤ 3

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Cálculo de posición - Primer ticket en cola vacía**
```gherkin
Given la cola PERSONAL_BANKER está vacía
When se crea un ticket "P01" para PERSONAL_BANKER
Then el sistema calcula:
  | Campo                | Valor |
  | positionInQueue      | 1     |
  | estimatedWaitMinutes | 15    |
And el cálculo es: 1 × 15min = 15min
```

**Escenario 2: Cálculo de posición - Cola con tickets existentes**
```gherkin
Given la cola EMPRESAS tiene tickets:
  | numero | status    | createdAt           |
  | E01    | EN_ESPERA | 2025-01-15 10:00:00 |
  | E02    | EN_ESPERA | 2025-01-15 10:05:00 |
  | E03    | EN_ESPERA | 2025-01-15 10:10:00 |
When se crea un ticket "E04" a las 10:15:00
Then el sistema calcula:
  | Campo                | Valor |
  | positionInQueue      | 4     |
  | estimatedWaitMinutes | 80    |
And el cálculo es: 4 × 20min = 80min
```

**Escenario 3: Recalculo automático - Ticket anterior completado**
```gherkin
Given la cola CAJA tiene tickets:
  | numero | status    | positionInQueue |
  | C01    | EN_ESPERA | 1               |
  | C02    | EN_ESPERA | 2               |
  | C03    | EN_ESPERA | 3               |
When el ticket "C01" cambia a estado COMPLETADO
Then el sistema recalcula automáticamente:
  | numero | positionInQueue | estimatedWaitMinutes |
  | C02    | 1               | 5                    |
  | C03    | 2               | 10                   |
```

**Escenario 4: Cambio a estado PROXIMO - Posición ≤ 3**
```gherkin
Given un ticket "G05" tiene positionInQueue = 4
And el ticket tiene status = EN_ESPERA
When otro ticket se completa y "G05" pasa a posición 3
Then el sistema actualiza:
  | Campo           | Valor   |
  | positionInQueue | 3       |
  | status          | PROXIMO |
And el sistema programa mensaje "totem_proximo_turno"
```

**Escenario 5: Consulta de posición vía API**
```gherkin
Given un ticket "P07" tiene positionInQueue = 5
When el cliente consulta GET /api/tickets/P07/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "P07",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 75,
    "queueType": "PERSONAL_BANKER",
    "status": "EN_ESPERA",
    "lastUpdated": "2025-01-15T10:30:00Z"
  }
```

**Postcondiciones:**
- Posición y tiempo actualizados en base de datos
- Estado cambiado a PROXIMO si posición ≤ 3
- Mensaje programado si cambio a PROXIMO
- Evento de auditoría registrado si hay cambios

**Endpoints HTTP:**
- `GET /api/tickets/{numero}/position` - Consultar posición actual

---

### RF-004: Asignar Ticket a Ejecutivo Automáticamente

**Descripción:** El sistema debe asignar automáticamente el siguiente ticket en cola cuando un ejecutivo se libere, considerando la prioridad de colas, balanceo de carga entre ejecutivos disponibles, y orden FIFO dentro de cada cola. La asignación debe ser inmediata y notificar tanto al cliente como al ejecutivo.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Al menos un ejecutivo con estado AVAILABLE
- Tickets en estado EN_ESPERA o PROXIMO en las colas
- Sistema de asignación operativo

**Modelo de Datos (Entidad Advisor):**
- id: BIGSERIAL (primary key)
- name: String, nombre completo del ejecutivo
- email: String, correo electrónico corporativo
- status: Enum (AVAILABLE, BUSY, OFFLINE)
- moduleNumber: Integer 1-5, número del módulo asignado
- assignedTicketsCount: Integer, contador de tickets asignados actualmente
- lastAssignedAt: Timestamp (nullable), última asignación recibida

**Algoritmo de Asignación:**

**1. Selección de Cola (por prioridad):**
```
FOR cada cola en orden de prioridad (GERENCIA=4, EMPRESAS=3, PERSONAL_BANKER=2, CAJA=1):
  IF cola tiene tickets EN_ESPERA o PROXIMO:
    RETURN cola
```

**2. Selección de Ticket (FIFO dentro de cola):**
```
ticket = SELECT TOP 1 FROM tickets 
         WHERE queueType = colaSeleccionada 
         AND status IN (EN_ESPERA, PROXIMO)
         ORDER BY createdAt ASC
```

**3. Selección de Ejecutivo (balanceo de carga):**
```
ejecutivo = SELECT TOP 1 FROM advisors 
            WHERE status = AVAILABLE 
            ORDER BY assignedTicketsCount ASC, lastAssignedAt ASC
```

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA)
- RN-003: Orden FIFO dentro de cola (createdAt menor primero)
- RN-004: Balanceo de carga (menor assignedTicketsCount primero)
- RN-011: Auditoría obligatoria de asignaciones
- RN-013: Solo ejecutivos AVAILABLE reciben asignaciones

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Asignación exitosa - Un ejecutivo disponible**
```gherkin
Given hay un ejecutivo disponible:
  | name        | status    | moduleNumber | assignedTicketsCount |
  | Juan Pérez  | AVAILABLE | 1            | 0                    |
And hay un ticket en cola:
  | numero | queueType | status    | createdAt           |
  | C05    | CAJA      | EN_ESPERA | 2025-01-15 10:00:00 |
When el sistema procesa asignaciones automáticas
Then el sistema asigna el ticket "C05" al ejecutivo "Juan Pérez"
And el ticket se actualiza con:
  | Campo                | Valor       |
  | status               | ATENDIENDO  |
  | assignedAdvisor      | Juan Pérez  |
  | assignedModuleNumber | 1           |
And el ejecutivo se actualiza con:
  | Campo                | Valor                |
  | status               | BUSY                 |
  | assignedTicketsCount | 1                    |
  | lastAssignedAt       | timestamp actual     |
And el sistema envía mensaje "totem_es_tu_turno" al cliente
And el sistema registra evento de auditoría "TICKET_ASIGNADO"
```

**Escenario 2: Prioridad de colas - GERENCIA antes que CAJA**
```gherkin
Given hay tickets en múltiples colas:
  | numero | queueType | status    | createdAt           |
  | C01    | CAJA      | EN_ESPERA | 2025-01-15 09:00:00 |
  | G01    | GERENCIA  | EN_ESPERA | 2025-01-15 10:00:00 |
And hay un ejecutivo disponible:
  | name       | status    | assignedTicketsCount |
  | Ana López | AVAILABLE | 0                    |
When el sistema procesa asignaciones
Then el sistema asigna el ticket "G01" (GERENCIA) antes que "C01" (CAJA)
And el ticket "G01" cambia a estado ATENDIENDO
And el ticket "C01" permanece EN_ESPERA
```

**Escenario 3: Orden FIFO dentro de cola - Ticket más antiguo primero**
```gherkin
Given hay múltiples tickets en cola PERSONAL_BANKER:
  | numero | status    | createdAt           |
  | P03    | EN_ESPERA | 2025-01-15 10:15:00 |
  | P01    | EN_ESPERA | 2025-01-15 10:00:00 |
  | P02    | EN_ESPERA | 2025-01-15 10:10:00 |
And hay un ejecutivo disponible
When el sistema procesa asignaciones
Then el sistema asigna el ticket "P01" (más antiguo: 10:00:00)
And los tickets "P02" y "P03" permanecen EN_ESPERA
```

**Escenario 4: Balanceo de carga - Ejecutivo con menor carga**
```gherkin
Given hay múltiples ejecutivos disponibles:
  | name         | status    | assignedTicketsCount | lastAssignedAt      |
  | Carlos Ruiz  | AVAILABLE | 2                    | 2025-01-15 09:30:00 |
  | María Silva | AVAILABLE | 1                    | 2025-01-15 09:45:00 |
  | Luis Torres  | AVAILABLE | 1                    | 2025-01-15 09:20:00 |
And hay un ticket "E05" en cola EMPRESAS
When el sistema procesa asignaciones
Then el sistema selecciona a "Luis Torres" (menor assignedTicketsCount=1 y lastAssignedAt más antiguo)
And "Luis Torres" recibe la asignación
And su assignedTicketsCount se incrementa a 2
```

**Escenario 5: No hay ejecutivos disponibles - Ticket permanece en cola**
```gherkin
Given todos los ejecutivos están ocupados:
  | name        | status  | assignedTicketsCount |
  | Juan Pérez  | BUSY    | 1                    |
  | Ana López  | BUSY    | 1                    |
  | Carlos Ruiz | OFFLINE | 0                    |
And hay tickets esperando en colas
When el sistema procesa asignaciones
Then el sistema NO asigna ningún ticket
And todos los tickets permanecen EN_ESPERA o PROXIMO
And el sistema programa siguiente verificación en 30 segundos
```

**Escenario 6: Ejecutivo se libera - Asignación inmediata**
```gherkin
Given un ejecutivo "María Silva" está BUSY atendiendo ticket "P10"
And hay tickets esperando:
  | numero | queueType      | status    |
  | P11    | PERSONAL_BANKER| EN_ESPERA |
  | C08    | CAJA           | EN_ESPERA |
When el ticket "P10" cambia a estado COMPLETADO
Then el ejecutivo "María Silva" cambia a AVAILABLE automáticamente
And su assignedTicketsCount se decrementa a 0
And el sistema inmediatamente asigna el ticket "P11" a "María Silva"
And "María Silva" vuelve a estado BUSY
```

**Escenario 7: Ticket PROXIMO tiene prioridad sobre EN_ESPERA**
```gherkin
Given hay tickets en la misma cola:
  | numero | status    | positionInQueue | createdAt           |
  | P05    | EN_ESPERA | 5               | 2025-01-15 09:00:00 |
  | P08    | PROXIMO   | 2               | 2025-01-15 10:00:00 |
And hay un ejecutivo disponible
When el sistema procesa asignaciones
Then el sistema asigna el ticket "P08" (PROXIMO) antes que "P05" (EN_ESPERA)
And el criterio de selección es: status PROXIMO > EN_ESPERA, luego createdAt ASC
```

**Postcondiciones:**
- Ticket asignado con estado ATENDIENDO
- Ejecutivo marcado como BUSY
- assignedTicketsCount incrementado
- Mensaje "totem_es_tu_turno" enviado al cliente
- Evento de auditoría "TICKET_ASIGNADO" registrado

**Endpoints HTTP:**
- Ninguno (proceso interno automatizado)

---

### RF-005: Gestionar Múltiples Colas

**Descripción:** El sistema debe gestionar cuatro tipos de cola con diferentes características operacionales: tiempos promedio de atención, prioridades de asignación, y prefijos de numeración. Cada cola opera de forma independiente pero coordinada para optimizar el flujo de atención según el tipo de servicio requerido.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Sistema de colas inicializado
- Configuración de tipos de cola cargada
- Ejecutivos asignados a tipos de cola específicos

**Configuración de Colas:**

| Tipo de Cola | Display Name | Tiempo Promedio | Prioridad | Prefijo | Descripción |
|--------------|--------------|-----------------|-----------|---------|-------------|
| CAJA | Caja | 5 min | 1 (baja) | C | Transacciones básicas, depósitos, retiros |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 (media) | P | Productos financieros, créditos, inversiones |
| EMPRESAS | Empresas | 20 min | 3 (media-alta) | E | Clientes corporativos, servicios empresariales |
| GERENCIA | Gerencia | 30 min | 4 (máxima) | G | Casos especiales, reclamos, situaciones complejas |

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas para asignación automática
- RN-005: Formato de número de ticket por cola
- RN-006: Prefijos específicos por tipo de cola
- RN-010: Cálculo de tiempo estimado por cola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consulta de estado de cola específica**
```gherkin
Given la cola PERSONAL_BANKER tiene tickets:
  | numero | status    | positionInQueue | estimatedWaitMinutes |
  | P01    | ATENDIENDO| 0               | 0                    |
  | P02    | EN_ESPERA | 1               | 15                   |
  | P03    | EN_ESPERA | 2               | 30                   |
When se consulta GET /api/admin/queues/PERSONAL_BANKER
Then el sistema retorna HTTP 200 con JSON:
  {
    "queueType": "PERSONAL_BANKER",
    "displayName": "Personal Banker",
    "averageWaitMinutes": 15,
    "priority": 2,
    "prefix": "P",
    "totalTickets": 3,
    "waitingTickets": 2,
    "attendingTickets": 1,
    "estimatedWaitTime": 30,
    "lastUpdated": "2025-01-15T10:30:00Z"
  }
```

**Escenario 2: Estadísticas de todas las colas**
```gherkin
Given el sistema tiene tickets en múltiples colas:
  | queueType      | waitingCount | attendingCount | completedToday |
  | CAJA           | 5            | 2              | 45             |
  | PERSONAL_BANKER| 3            | 1              | 12             |
  | EMPRESAS       | 2            | 1              | 8              |
  | GERENCIA       | 1            | 0              | 3              |
When se consulta GET /api/admin/queues/stats
Then el sistema retorna estadísticas consolidadas:
  {
    "totalQueues": 4,
    "totalWaiting": 11,
    "totalAttending": 4,
    "totalCompletedToday": 68,
    "queues": [
      {
        "queueType": "CAJA",
        "waiting": 5,
        "attending": 2,
        "completed": 45,
        "averageWaitMinutes": 5
      },
      {
        "queueType": "PERSONAL_BANKER",
        "waiting": 3,
        "attending": 1,
        "completed": 12,
        "averageWaitMinutes": 15
      }
    ]
  }
```

**Escenario 3: Distribución de tickets por prioridad**
```gherkin
Given hay tickets esperando en todas las colas:
  | queueType      | waitingTickets | priority |
  | CAJA           | 8              | 1        |
  | PERSONAL_BANKER| 4              | 2        |
  | EMPRESAS       | 2              | 3        |
  | GERENCIA       | 1              | 4        |
And hay 3 ejecutivos disponibles
When el sistema procesa asignaciones automáticas
Then el orden de asignación es:
  | Orden | queueType | Razón |
  | 1     | GERENCIA  | Prioridad 4 (máxima) |
  | 2     | EMPRESAS  | Prioridad 3 |
  | 3     | PERSONAL_BANKER | Prioridad 2 |
And los tickets de CAJA (prioridad 1) se asignan al final
```

**Escenario 4: Generación de números por cola**
```gherkin
Given es un nuevo día y los contadores están reseteados
When se crean tickets en diferentes colas:
  | Orden | queueType      | Número Esperado |
  | 1     | CAJA           | C01             |
  | 2     | GERENCIA       | G01             |
  | 3     | CAJA           | C02             |
  | 4     | EMPRESAS       | E01             |
  | 5     | PERSONAL_BANKER| P01             |
Then cada cola mantiene su secuencia independiente
And los prefijos corresponden a: C=Caja, G=Gerencia, E=Empresas, P=Personal Banker
```

**Escenario 5: Cálculo de tiempo estimado por cola**
```gherkin
Given un cliente está en posición 4 en diferentes colas:
When se calcula el tiempo estimado para cada cola:
  | queueType      | posición | tiempoPromedio | tiempoEstimado |
  | CAJA           | 4        | 5 min          | 20 min         |
  | PERSONAL_BANKER| 4        | 15 min         | 60 min         |
  | EMPRESAS       | 4        | 20 min         | 80 min         |
  | GERENCIA       | 4        | 30 min         | 120 min        |
Then cada cola aplica su tiempo promedio específico
And el cálculo es: posición × tiempoPromedioCola
```

**Postcondiciones:**
- Cada cola mantiene su configuración independiente
- Estadísticas actualizadas en tiempo real
- Numeración secuencial por cola
- Tiempos estimados calculados correctamente

**Endpoints HTTP:**
- `GET /api/admin/queues/{type}` - Consultar estado de cola específica
- `GET /api/admin/queues/{type}/stats` - Estadísticas de cola específica

---

### RF-006: Consultar Estado del Ticket

**Descripción:** El sistema debe permitir al cliente consultar en cualquier momento el estado actual de su ticket, mostrando información actualizada sobre posición en cola, tiempo estimado de espera, estado actual, y ejecutivo asignado si aplica. La consulta puede realizarse por UUID o por número de ticket.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Ticket existe en el sistema
- Cliente conoce el UUID o número del ticket
- API de consultas disponible

**Información Retornada:**
- Número de ticket
- Estado actual (EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, etc.)
- Posición en cola (si aplica)
- Tiempo estimado de espera actualizado
- Tipo de cola
- Ejecutivo asignado (si aplica)
- Número de módulo (si aplica)
- Timestamp de última actualización

**Reglas de Negocio Aplicables:**
- RN-009: Estados válidos de ticket
- RN-010: Cálculo de tiempo estimado actualizado
- RN-012: Estado PROXIMO cuando posición ≤ 3

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consulta exitosa por UUID - Ticket EN_ESPERA**
```gherkin
Given existe un ticket con:
  | Campo                | Valor                                    |
  | codigoReferencia     | a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6    |
  | numero               | P05                                      |
  | status               | EN_ESPERA                                |
  | queueType            | PERSONAL_BANKER                          |
  | positionInQueue      | 3                                        |
  | estimatedWaitMinutes | 45                                       |
  | assignedAdvisor      | null                                     |
When el cliente consulta GET /api/tickets/a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6
Then el sistema retorna HTTP 200 con JSON:
  {
    "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "numero": "P05",
    "status": "EN_ESPERA",
    "queueType": "PERSONAL_BANKER",
    "positionInQueue": 3,
    "estimatedWaitMinutes": 45,
    "assignedAdvisor": null,
    "assignedModuleNumber": null,
    "createdAt": "2025-01-15T10:00:00Z",
    "lastUpdated": "2025-01-15T10:30:00Z"
  }
```

**Escenario 2: Consulta por número - Ticket ATENDIENDO**
```gherkin
Given existe un ticket con:
  | Campo                | Valor        |
  | numero               | E03          |
  | status               | ATENDIENDO   |
  | queueType            | EMPRESAS     |
  | assignedAdvisor      | Ana López    |
  | assignedModuleNumber | 2            |
  | positionInQueue      | 0            |
When el cliente consulta GET /api/tickets/E03
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "E03",
    "status": "ATENDIENDO",
    "queueType": "EMPRESAS",
    "positionInQueue": 0,
    "estimatedWaitMinutes": 0,
    "assignedAdvisor": "Ana López",
    "assignedModuleNumber": 2,
    "message": "Dirígete al módulo 2. Te atiende Ana López."
  }
```

**Escenario 3: Consulta - Ticket COMPLETADO**
```gherkin
Given existe un ticket completado:
  | Campo                | Valor                |
  | numero               | C08                  |
  | status               | COMPLETADO           |
  | queueType            | CAJA                 |
  | assignedAdvisor      | Carlos Ruiz          |
  | completedAt          | 2025-01-15T11:45:00Z |
When el cliente consulta GET /api/tickets/C08
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "C08",
    "status": "COMPLETADO",
    "queueType": "CAJA",
    "assignedAdvisor": "Carlos Ruiz",
    "assignedModuleNumber": 1,
    "completedAt": "2025-01-15T11:45:00Z",
    "message": "Tu atención ha sido completada exitosamente."
  }
```

**Escenario 4: Error - Ticket no existe**
```gherkin
Given no existe ningún ticket con UUID "invalid-uuid-12345"
When el cliente consulta GET /api/tickets/invalid-uuid-12345
Then el sistema retorna HTTP 404 Not Found con JSON:
  {
    "error": "TICKET_NO_ENCONTRADO",
    "mensaje": "No se encontró un ticket con el identificador proporcionado",
    "codigo": "invalid-uuid-12345"
  }
```

**Escenario 5: Consulta con información actualizada en tiempo real**
```gherkin
Given un ticket "G02" tiene positionInQueue = 4
And otro ticket se completa, moviendo "G02" a posición 3
When el cliente consulta GET /api/tickets/G02 inmediatamente después
Then el sistema retorna la información actualizada:
  {
    "numero": "G02",
    "status": "PROXIMO",
    "positionInQueue": 3,
    "estimatedWaitMinutes": 90,
    "queueType": "GERENCIA",
    "message": "Próximo a ser atendido. Por favor, acércate a la sucursal."
  }
And el estado cambió automáticamente a PROXIMO (posición ≤ 3)
```

**Postcondiciones:**
- Información actualizada retornada al cliente
- Sin modificación del estado del ticket
- Timestamp de consulta registrado (opcional)

**Endpoints HTTP:**
- `GET /api/tickets/{codigoReferencia}` - Consultar por UUID
- `GET /api/tickets/{numero}` - Consultar por número de ticket

---

### RF-007: Panel de Monitoreo para Supervisor

**Descripción:** El sistema debe proveer un dashboard en tiempo real que muestre información consolidada sobre el estado operacional: resumen de tickets por estado, cantidad de clientes en espera por cola, estado de ejecutivos, tiempos promedio de atención, y alertas de situaciones críticas. La información debe actualizarse automáticamente cada 5 segundos.

**Prioridad:** Alta

**Actor Principal:** Supervisor

**Precondiciones:**
- Usuario con permisos de supervisor autenticado
- Sistema operativo con datos disponibles
- Dashboard web funcional

**Información del Dashboard:**

**Resumen General:**
- Total de tickets por estado (EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO)
- Clientes en espera por cola
- Ejecutivos disponibles/ocupados/offline
- Tiempo promedio de atención del día
- Alertas críticas

**Métricas por Cola:**
- Tickets esperando por cola
- Tiempo de espera máximo actual
- Tickets completados hoy
- Tiempo promedio de atención

**Estado de Ejecutivos:**
- Lista de ejecutivos con estado actual
- Módulo asignado
- Tickets atendidos hoy
- Tiempo en estado actual

**Reglas de Negocio Aplicables:**
- RN-009: Estados válidos de tickets
- RN-013: Estados válidos de ejecutivos
- RN-011: Auditoría para trazabilidad de métricas

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Dashboard principal - Resumen general**
```gherkin
Given el sistema tiene la siguiente información:
  | Métrica           | Valor |
  | Tickets EN_ESPERA   | 12    |
  | Tickets PROXIMO     | 4     |
  | Tickets ATENDIENDO  | 5     |
  | Tickets COMPLETADOS | 87    |
  | Ejecutivos AVAILABLE| 2     |
  | Ejecutivos BUSY     | 5     |
  | Ejecutivos OFFLINE  | 1     |
When el supervisor accede a GET /api/admin/dashboard
Then el sistema retorna HTTP 200 con JSON:
  {
    "timestamp": "2025-01-15T14:30:00Z",
    "summary": {
      "totalTicketsToday": 108,
      "waitingTickets": 16,
      "attendingTickets": 5,
      "completedTickets": 87,
      "averageWaitTimeMinutes": 18,
      "peakHour": "11:00-12:00"
    },
    "advisors": {
      "available": 2,
      "busy": 5,
      "offline": 1,
      "totalCapacity": 8
    },
    "alerts": [
      {
        "type": "HIGH_WAIT_TIME",
        "message": "Cola GERENCIA con tiempo de espera superior a 60 minutos",
        "severity": "WARNING"
      }
    ]
  }
```

**Escenario 2: Estado detallado de colas**
```gherkin
Given las colas tienen el siguiente estado:
  | queueType      | waiting | attending | completed | maxWaitMinutes |
  | CAJA           | 8       | 2         | 45        | 25             |
  | PERSONAL_BANKER| 4       | 1         | 18        | 45             |
  | EMPRESAS       | 3       | 1         | 12        | 60             |
  | GERENCIA       | 1       | 1         | 5         | 90             |
When el supervisor consulta GET /api/admin/summary
Then el sistema retorna información detallada por cola:
  {
    "queues": [
      {
        "queueType": "CAJA",
        "displayName": "Caja",
        "waitingTickets": 8,
        "attendingTickets": 2,
        "completedToday": 45,
        "maxWaitTimeMinutes": 25,
        "averageServiceMinutes": 4,
        "status": "NORMAL"
      },
      {
        "queueType": "GERENCIA",
        "displayName": "Gerencia",
        "waitingTickets": 1,
        "attendingTickets": 1,
        "completedToday": 5,
        "maxWaitTimeMinutes": 90,
        "averageServiceMinutes": 28,
        "status": "CRITICAL"
      }
    ]
  }
```

**Escenario 3: Estado de ejecutivos**
```gherkin
Given hay ejecutivos con diferentes estados:
  | name         | status    | moduleNumber | currentTicket | timeInStatus |
  | Juan Pérez   | BUSY      | 1            | C15           | 8 min        |
  | Ana López    | AVAILABLE | 2            | null          | 3 min        |
  | Carlos Ruiz  | BUSY      | 3            | P08           | 12 min       |
  | María Silva | OFFLINE   | 4            | null          | 45 min       |
When el supervisor consulta GET /api/admin/advisors
Then el sistema retorna el estado de todos los ejecutivos:
  {
    "advisors": [
      {
        "id": 1,
        "name": "Juan Pérez",
        "status": "BUSY",
        "moduleNumber": 1,
        "currentTicket": "C15",
        "timeInCurrentStatus": "8 min",
        "ticketsCompletedToday": 12,
        "averageServiceMinutes": 5
      },
      {
        "id": 2,
        "name": "Ana López",
        "status": "AVAILABLE",
        "moduleNumber": 2,
        "currentTicket": null,
        "timeInCurrentStatus": "3 min",
        "ticketsCompletedToday": 8,
        "averageServiceMinutes": 14
      }
    ]
  }
```

**Escenario 4: Alertas críticas - Cola con más de 15 esperando**
```gherkin
Given la cola CAJA tiene 18 tickets esperando
And el umbral de alerta es 15 tickets
When el sistema evalúa alertas críticas
Then el sistema genera alerta:
  {
    "type": "QUEUE_OVERLOAD",
    "queueType": "CAJA",
    "message": "Cola CAJA crítica: 18 clientes esperando (límite: 15)",
    "severity": "CRITICAL",
    "timestamp": "2025-01-15T14:35:00Z",
    "suggestedAction": "Asignar ejecutivos adicionales a CAJA"
  }
And la alerta aparece en el dashboard
```

**Escenario 5: Cambio de estado de ejecutivo desde dashboard**
```gherkin
Given un ejecutivo "Luis Torres" está en estado OFFLINE
And el supervisor tiene permisos administrativos
When el supervisor envía PUT /api/admin/advisors/3/status con:
  {
    "status": "AVAILABLE",
    "reason": "Regreso de almuerzo"
  }
Then el sistema actualiza el estado del ejecutivo:
  | Campo  | Valor Anterior | Valor Nuevo |
  | status | OFFLINE        | AVAILABLE   |
And el sistema registra evento de auditoría "ADVISOR_STATUS_CHANGED"
And el dashboard se actualiza automáticamente
And el ejecutivo queda disponible para recibir asignaciones
```

**Escenario 6: Actualización automática cada 5 segundos**
```gherkin
Given el supervisor tiene el dashboard abierto
And la última actualización fue a las 14:30:00
When transcurren 5 segundos (14:30:05)
Then el dashboard solicita automáticamente datos actualizados
And muestra la información más reciente
And el timestamp se actualiza a "2025-01-15T14:30:05Z"
And los cambios se reflejan sin intervención del usuario
```

**Postcondiciones:**
- Dashboard actualizado con información en tiempo real
- Alertas críticas visibles para el supervisor
- Estados de ejecutivos actualizados
- Eventos de cambios registrados en auditoría

**Endpoints HTTP:**
- `GET /api/admin/dashboard` - Dashboard principal
- `GET /api/admin/summary` - Resumen consolidado
- `GET /api/admin/advisors` - Estado de ejecutivos
- `GET /api/admin/advisors/stats` - Estadísticas de ejecutivos
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de ejecutivo

---

### RF-008: Registrar Auditoría de Eventos

**Descripción:** El sistema debe registrar automáticamente todos los eventos relevantes del proceso de gestión de tickets: creación de tickets, asignaciones, cambios de estado, envío de mensajes, y acciones de usuarios. La información debe incluir timestamp, tipo de evento, actor involucrado, entidad afectada, y cambios de estado para garantizar trazabilidad completa.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Sistema de auditoría inicializado
- Base de datos de auditoría disponible
- Eventos del sistema operando normalmente

**Modelo de Datos (Entidad AuditLog):**
- id: BIGSERIAL (primary key)
- timestamp: Timestamp, fecha y hora del evento
- eventType: String, tipo de evento (TICKET_CREADO, TICKET_ASIGNADO, etc.)
- actor: String, quién ejecutó la acción (SYSTEM, USER, SCHEDULER)
- entityType: String, tipo de entidad afectada (TICKET, ADVISOR, MESSAGE)
- entityId: String, identificador de la entidad afectada
- previousState: JSON (nullable), estado anterior de la entidad
- newState: JSON (nullable), nuevo estado de la entidad
- additionalData: JSON (nullable), información adicional del contexto

**Tipos de Eventos Auditados:**

| Evento | Descripción | Actor | Entidad |
|--------|-------------|-------|----------|
| TICKET_CREADO | Ticket creado por cliente | SYSTEM | TICKET |
| TICKET_ASIGNADO | Ticket asignado a ejecutivo | SYSTEM | TICKET |
| TICKET_COMPLETADO | Ticket marcado como completado | SYSTEM | TICKET |
| MENSAJE_ENVIADO | Mensaje enviado vía Telegram | SYSTEM | MESSAGE |
| MENSAJE_FALLIDO | Fallo en envío de mensaje | SYSTEM | MESSAGE |
| ADVISOR_STATUS_CHANGED | Cambio de estado de ejecutivo | USER/SYSTEM | ADVISOR |
| POSITION_UPDATED | Actualización de posición en cola | SYSTEM | TICKET |
| QUEUE_ALERT_GENERATED | Alerta crítica generada | SYSTEM | QUEUE |

**Reglas de Negocio Aplicables:**
- RN-011: Auditoría obligatoria para todos los eventos críticos
- Retención de logs por 12 meses mínimo
- Integridad de datos de auditoría (no modificables)

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Auditoría de creación de ticket**
```gherkin
Given un cliente crea un ticket con datos:
  | Campo        | Valor           |
  | nationalId   | 12345678-9      |
  | queueType    | PERSONAL_BANKER |
  | numero       | P15             |
When el sistema completa la creación del ticket
Then el sistema registra evento de auditoría:
  | Campo         | Valor                    |
  | eventType     | TICKET_CREADO            |
  | actor         | SYSTEM                   |
  | entityType    | TICKET                   |
  | entityId      | P15                      |
  | previousState | null                     |
  | newState      | {"status": "EN_ESPERA", "queueType": "PERSONAL_BANKER"} |
And el timestamp es la fecha/hora actual
And el registro es inmutable
```

**Escenario 2: Auditoría de asignación de ticket**
```gherkin
Given un ticket "E05" está EN_ESPERA
And se asigna al ejecutivo "Juan Pérez" en módulo 3
When el sistema completa la asignación
Then el sistema registra evento de auditoría:
  {
    "eventType": "TICKET_ASIGNADO",
    "actor": "SYSTEM",
    "entityType": "TICKET",
    "entityId": "E05",
    "previousState": {
      "status": "EN_ESPERA",
      "assignedAdvisor": null,
      "assignedModuleNumber": null
    },
    "newState": {
      "status": "ATENDIENDO",
      "assignedAdvisor": "Juan Pérez",
      "assignedModuleNumber": 3
    },
    "additionalData": {
      "assignmentAlgorithm": "PRIORITY_FIFO_BALANCE",
      "queueType": "EMPRESAS"
    }
  }
```

**Escenario 3: Auditoría de envío de mensaje**
```gherkin
Given un mensaje "totem_ticket_creado" se envía exitosamente
And Telegram retorna message_id "67890"
When el sistema completa el envío
Then el sistema registra evento de auditoría:
  {
    "eventType": "MENSAJE_ENVIADO",
    "actor": "SYSTEM",
    "entityType": "MESSAGE",
    "entityId": "msg_12345",
    "previousState": {
      "estadoEnvio": "PENDIENTE",
      "intentos": 1
    },
    "newState": {
      "estadoEnvio": "ENVIADO",
      "telegramMessageId": "67890",
      "intentos": 1
    },
    "additionalData": {
      "plantilla": "totem_ticket_creado",
      "ticketNumber": "C08"
    }
  }
```

**Escenario 4: Auditoría de cambio de estado de ejecutivo**
```gherkin
Given un supervisor cambia el estado de "Ana López" de OFFLINE a AVAILABLE
And el supervisor es "admin@banco.com"
When el sistema procesa el cambio
Then el sistema registra evento de auditoría:
  {
    "eventType": "ADVISOR_STATUS_CHANGED",
    "actor": "USER:admin@banco.com",
    "entityType": "ADVISOR",
    "entityId": "advisor_2",
    "previousState": {
      "status": "OFFLINE",
      "assignedTicketsCount": 0
    },
    "newState": {
      "status": "AVAILABLE",
      "assignedTicketsCount": 0
    },
    "additionalData": {
      "reason": "Regreso de almuerzo",
      "moduleNumber": 2
    }
  }
```

**Escenario 5: Consulta de auditoría por entidad**
```gherkin
Given existen eventos de auditoría para el ticket "P10":
  | eventType        | timestamp           | actor  |
  | TICKET_CREADO    | 2025-01-15 10:00:00 | SYSTEM |
  | POSITION_UPDATED | 2025-01-15 10:15:00 | SYSTEM |
  | TICKET_ASIGNADO  | 2025-01-15 10:30:00 | SYSTEM |
When se consulta GET /api/admin/audit/ticket/P10
Then el sistema retorna el historial completo:
  {
    "entityId": "P10",
    "entityType": "TICKET",
    "totalEvents": 3,
    "events": [
      {
        "timestamp": "2025-01-15T10:00:00Z",
        "eventType": "TICKET_CREADO",
        "actor": "SYSTEM",
        "newState": {"status": "EN_ESPERA"}
      },
      {
        "timestamp": "2025-01-15T10:30:00Z",
        "eventType": "TICKET_ASIGNADO",
        "actor": "SYSTEM",
        "newState": {"status": "ATENDIENDO"}
      }
    ]
  }
```

**Postcondiciones:**
- Evento registrado en tabla de auditoría
- Registro inmutable y con timestamp preciso
- Información completa de cambios de estado
- Trazabilidad completa disponible para análisis

**Endpoints HTTP:**
- `GET /api/admin/audit/ticket/{id}` - Auditoría de ticket específico
- `GET /api/admin/audit/advisor/{id}` - Auditoría de ejecutivo específico
- `GET /api/admin/audit/events` - Consulta general de eventos con filtros

---

## 5. Matriz de Trazabilidad

### 5.1 Matriz RF → Beneficio → Endpoints

| RF | Requerimiento | Beneficio de Negocio | Endpoints HTTP |
|----|---------------|---------------------|----------------|
| RF-001 | Crear Ticket Digital | Digitalización del proceso, eliminación de tickets físicos | `POST /api/tickets` |
| RF-002 | Notificaciones Telegram | Movilidad del cliente, reducción de abandonos | Ninguno (automatizado) |
| RF-003 | Calcular Posición y Tiempo | Transparencia en tiempos de espera | `GET /api/tickets/{numero}/position` |
| RF-004 | Asignar Ticket Automáticamente | Optimización de recursos, balanceo de carga | Ninguno (automatizado) |
| RF-005 | Gestionar Múltiples Colas | Segmentación por tipo de servicio | `GET /api/admin/queues/{type}`, `GET /api/admin/queues/{type}/stats` |
| RF-006 | Consultar Estado Ticket | Autoservicio del cliente, reducción de consultas | `GET /api/tickets/{uuid}`, `GET /api/tickets/{numero}` |
| RF-007 | Panel de Monitoreo | Supervisión operacional, toma de decisiones | `GET /api/admin/dashboard`, `GET /api/admin/summary`, `GET /api/admin/advisors` |
| RF-008 | Auditoría de Eventos | Trazabilidad completa, cumplimiento normativo | `GET /api/admin/audit/ticket/{id}`, `GET /api/admin/audit/events` |

### 5.2 Matriz de Dependencias entre RFs

| RF Origen | RF Dependiente | Tipo de Dependencia | Descripción |
|-----------|----------------|--------------------|--------------|
| RF-001 | RF-002 | Secuencial | Ticket debe existir para enviar notificaciones |
| RF-001 | RF-003 | Simultánea | Posición se calcula al crear ticket |
| RF-003 | RF-002 | Condicional | Mensaje 2 se envía cuando posición ≤ 3 |
| RF-004 | RF-002 | Secuencial | Mensaje 3 se envía tras asignación |
| RF-001,RF-004 | RF-008 | Transversal | Todos los eventos se auditan |
| RF-005 | RF-001,RF-003,RF-004 | Estructural | Colas son base para otros RFs |
| RF-001,RF-003,RF-004 | RF-007 | Agregación | Dashboard consolida información de otros RFs |

## 6. Modelo de Datos Consolidado

### 6.1 Entidades Principales

**Ticket (12 campos):**
- codigoReferencia: UUID
- numero: String
- nationalId: String
- telefono: String
- branchOffice: String
- queueType: Enum
- status: Enum
- positionInQueue: Integer
- estimatedWaitMinutes: Integer
- createdAt: Timestamp
- assignedAdvisor: FK a Advisor
- assignedModuleNumber: Integer

**Mensaje (8 campos):**
- id: BIGSERIAL
- ticket_id: FK a Ticket
- plantilla: String
- estadoEnvio: Enum
- fechaProgramada: Timestamp
- fechaEnvio: Timestamp
- telegramMessageId: String
- intentos: Integer

**Advisor (6 campos):**
- id: BIGSERIAL
- name: String
- email: String
- status: Enum
- moduleNumber: Integer
- assignedTicketsCount: Integer
- lastAssignedAt: Timestamp

**AuditLog (8 campos):**
- id: BIGSERIAL
- timestamp: Timestamp
- eventType: String
- actor: String
- entityType: String
- entityId: String
- previousState: JSON
- newState: JSON
- additionalData: JSON

### 6.2 Enumeraciones

- **QueueType:** CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA
- **TicketStatus:** EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO
- **AdvisorStatus:** AVAILABLE, BUSY, OFFLINE
- **MessageTemplate:** totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno

## 7. Casos de Uso Principales

### CU-001: Flujo Completo de Atención
1. Cliente crea ticket (RF-001)
2. Sistema calcula posición (RF-003)
3. Sistema envía Mensaje 1 (RF-002)
4. Sistema actualiza posición cuando otros avanzan (RF-003)
5. Sistema envía Mensaje 2 cuando posición ≤ 3 (RF-002)
6. Sistema asigna a ejecutivo disponible (RF-004)
7. Sistema envía Mensaje 3 (RF-002)
8. Todos los eventos se auditan (RF-008)

### CU-002: Supervisión Operacional
1. Supervisor accede al dashboard (RF-007)
2. Consulta estado de colas (RF-005)
3. Monitorea ejecutivos (RF-007)
4. Recibe alertas críticas (RF-007)
5. Cambia estado de ejecutivos (RF-007)
6. Consulta auditoría para análisis (RF-008)

### CU-003: Autoservicio del Cliente
1. Cliente consulta estado de su ticket (RF-006)
2. Sistema muestra posición actualizada (RF-003)
3. Cliente recibe notificaciones automáticas (RF-002)
4. Cliente se presenta cuando es su turno

## 8. Matriz de Endpoints HTTP

| Método | Endpoint | RF | Descripción |
|--------|----------|----|--------------|
| POST | `/api/tickets` | RF-001 | Crear nuevo ticket |
| GET | `/api/tickets/{uuid}` | RF-006 | Consultar ticket por UUID |
| GET | `/api/tickets/{numero}` | RF-006 | Consultar ticket por número |
| GET | `/api/tickets/{numero}/position` | RF-003 | Consultar posición actual |
| GET | `/api/admin/dashboard` | RF-007 | Dashboard principal |
| GET | `/api/admin/summary` | RF-007 | Resumen consolidado |
| GET | `/api/admin/advisors` | RF-007 | Estado de ejecutivos |
| GET | `/api/admin/advisors/stats` | RF-007 | Estadísticas de ejecutivos |
| PUT | `/api/admin/advisors/{id}/status` | RF-007 | Cambiar estado ejecutivo |
| GET | `/api/admin/queues/{type}` | RF-005 | Estado de cola específica |
| GET | `/api/admin/queues/{type}/stats` | RF-005 | Estadísticas de cola |
| GET | `/api/admin/audit/ticket/{id}` | RF-008 | Auditoría de ticket |
| GET | `/api/admin/audit/advisor/{id}` | RF-008 | Auditoría de ejecutivo |
| GET | `/api/admin/audit/events` | RF-008 | Consulta general de auditoría |
| GET | `/api/health` | - | Health check del sistema |

**Total: 15 endpoints HTTP**

## 9. Validaciones y Reglas de Formato

### 9.1 Formatos de Validación

- **RUT/ID Nacional:** Formato válido según país (ej: 12345678-9)
- **Teléfono:** Formato internacional +56XXXXXXXXX
- **Número de Ticket:** [Prefijo][01-99] (ej: C01, P15, E03, G02)
- **UUID:** Formato estándar UUID v4
- **Email:** Formato RFC 5322 para ejecutivos

### 9.2 Reglas de Negocio Transversales

- **RN-001:** Un cliente = 1 ticket activo máximo
- **RN-002:** Prioridad colas: GERENCIA(4) > EMPRESAS(3) > PERSONAL_BANKER(2) > CAJA(1)
- **RN-003:** Orden FIFO dentro de cada cola
- **RN-004:** Balanceo de carga entre ejecutivos
- **RN-010:** tiempoEstimado = posición × tiempoPromedio
- **RN-011:** Auditoría obligatoria de eventos críticos
- **RN-012:** Mensaje pre-aviso cuando posición ≤ 3

## 10. Checklist de Validación Final

### 10.1 Completitud
- ✅ 8 Requerimientos Funcionales documentados
- ✅ 47 Escenarios Gherkin totales
- ✅ 13 Reglas de Negocio numeradas
- ✅ 15 Endpoints HTTP mapeados
- ✅ 4 Entidades de datos definidas
- ✅ 4 Enumeraciones especificadas

### 10.2 Calidad
- ✅ Formato Gherkin correcto (Given/When/Then/And)
- ✅ Ejemplos JSON en respuestas HTTP
- ✅ Sin ambigüedades en descripciones
- ✅ Precondiciones y postcondiciones claras
- ✅ Reglas de negocio aplicadas consistentemente

### 10.3 Trazabilidad
- ✅ Matriz RF → Beneficio → Endpoints
- ✅ Matriz de dependencias entre RFs
- ✅ Casos de uso principales documentados
- ✅ Modelo de datos consolidado
- ✅ Auditoría completa garantizada

## 11. Glosario

| Término | Definición |
|---------|------------|
| **Ticket** | Turno digital asignado a un cliente para ser atendido |
| **Cola** | Fila virtual de tickets esperando atención |
| **Asesor** | Ejecutivo bancario que atiende clientes |
| **Módulo** | Estación de trabajo de un asesor (numerados 1-5) |
| **Chat ID** | Identificador único de usuario en Telegram |
| **UUID** | Identificador único universal para tickets |
| **FIFO** | First In, First Out - Primero en entrar, primero en salir |
| **Backoff Exponencial** | Incremento progresivo de tiempo entre reintentos |
| **Dashboard** | Panel de control con métricas en tiempo real |
| **Auditoría** | Registro inmutable de eventos del sistema |

---

# 🔌 Documentación de API REST - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Información General](#información-general)
2. [Gestión de Tickets](#gestión-de-tickets)
3. [Dashboard Administrativo](#dashboard-administrativo)
4. [Gestión de Asesores](#gestión-de-asesores)
5. [Estado de Colas](#estado-de-colas)
6. [Endpoints de Debug](#endpoints-de-debug)
7. [Códigos de Estado HTTP](#códigos-de-estado-http)
8. [Modelos de Datos](#modelos-de-datos)
9. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 🌐 Información General

### URL Base
```
http://localhost:8090
```

### Headers Comunes
```http
Content-Type: application/json
Accept: application/json
```

### CORS
- **Orígenes permitidos**: `http://localhost:3000`, `http://localhost:8080`
- **Max Age**: 3600 segundos

---

## 🎫 Gestión de Tickets

### Crear Ticket

**Endpoint**: `POST /api/tickets`

**Descripción**: Crea un nuevo ticket en el sistema y programa notificaciones automáticas.

#### Request Body
```json
{
  "nationalId": "12345678-9",
  "telefono": "+56987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```

#### Validaciones
- `nationalId`: **Obligatorio**, no vacío
- `telefono`: **Opcional**, formato `+56XXXXXXXXX`
- `branchOffice`: **Obligatorio**, no vacío
- `queueType`: **Obligatorio**, valores: `CAJA`, `PERSONAL_BANKER`, `EMPRESAS`, `GERENCIA`

#### Response (201 Created)
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "numero": "C001",
  "nationalId": "12345678-9",
  "telefono": "+56987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "positionInQueue": 3,
  "estimatedWaitMinutes": 15,
  "assignedAdvisor": null,
  "assignedModuleNumber": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Ejemplo cURL
```bash
curl -X POST http://localhost:8090/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "telefono": "+56987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }'
```

---

### Listar Tickets Activos

**Endpoint**: `GET /api/tickets`

**Descripción**: Obtiene todos los tickets en estado activo (EN_ESPERA, PROXIMO, ATENDIENDO).

#### Response (200 OK)
```json
[
  {
    "codigoReferencia": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "numero": "C001",
    "nationalId": "12345678-9",
    "telefono": "+56987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA",
    "status": "EN_ESPERA",
    "positionInQueue": 1,
    "estimatedWaitMinutes": 5,
    "assignedAdvisor": null,
    "assignedModuleNumber": null,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:35:00"
  },
  {
    "codigoReferencia": "b2c3d4e5-f6g7-8901-bcde-f23456789012",
    "numero": "P001",
    "nationalId": "98765432-1",
    "telefono": "+56912345678",
    "branchOffice": "Sucursal Norte",
    "queueType": "PERSONAL_BANKER",
    "status": "ATENDIENDO",
    "positionInQueue": 0,
    "estimatedWaitMinutes": 0,
    "assignedAdvisor": "María González",
    "assignedModuleNumber": 3,
    "createdAt": "2024-01-15T09:45:00",
    "updatedAt": "2024-01-15T10:15:00"
  }
]
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/tickets
```

---

### Obtener Ticket por Número

**Endpoint**: `GET /api/tickets/{numero}`

**Descripción**: Busca un ticket específico por su número.

#### Parámetros
- `numero`: Número del ticket (ej: C001, P015, E003, G001)

#### Response (200 OK)
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "numero": "C001",
  "nationalId": "12345678-9",
  "telefono": "+56987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "positionInQueue": 2,
  "estimatedWaitMinutes": 10,
  "assignedAdvisor": null,
  "assignedModuleNumber": null,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:40:00"
}
```

#### Response (404 Not Found)
```json
{
  "timestamp": "2024-01-15T10:45:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket no encontrado",
  "path": "/api/tickets/C999"
}
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/tickets/C001
```

---

### Llamar Ticket

**Endpoint**: `PUT /api/tickets/{ticketId}/llamar/{advisorId}`

**Descripción**: Asigna un ticket a un asesor y cambia su estado a ATENDIENDO.

#### Parámetros
- `ticketId`: ID numérico del ticket
- `advisorId`: ID numérico del asesor

#### Response (200 OK)
```
Status: 200 OK
Body: (vacío)
```

#### Efectos del Endpoint
1. Ticket cambia a estado `ATENDIENDO`
2. Asesor cambia a estado `BUSY`
3. Se asigna módulo al ticket
4. Se envía notificación "Es tu turno" por Telegram
5. Se actualiza contador de tickets del asesor

#### Ejemplo cURL
```bash
curl -X PUT http://localhost:8090/api/tickets/123/llamar/5
```

---

### Finalizar Ticket

**Endpoint**: `PUT /api/tickets/{ticketId}/finalizar`

**Descripción**: Marca un ticket como completado y libera al asesor.

#### Parámetros
- `ticketId`: ID numérico del ticket

#### Response (200 OK)
```
Status: 200 OK
Body: (vacío)
```

#### Efectos del Endpoint
1. Ticket cambia a estado `COMPLETADO`
2. Asesor vuelve a estado `AVAILABLE`
3. Se actualiza posición de tickets en cola
4. Se recalculan tiempos estimados

#### Ejemplo cURL
```bash
curl -X PUT http://localhost:8090/api/tickets/123/finalizar
```

---

## 📊 Dashboard Administrativo

### Obtener Dashboard

**Endpoint**: `GET /api/admin/dashboard`

**Descripción**: Obtiene métricas completas del sistema en tiempo real.

#### Response (200 OK)
```json
{
  "timestamp": "2024-01-15T14:30:00",
  "summary": {
    "totalTicketsToday": 45,
    "waitingTickets": 12,
    "attendingTickets": 8,
    "completedTickets": 25,
    "averageWaitTimeMinutes": 18,
    "peakHour": "11:00-12:00"
  },
  "advisors": {
    "available": 3,
    "busy": 5,
    "offline": 2,
    "totalCapacity": 10
  },
  "queues": [
    {
      "queueType": "CAJA",
      "displayName": "Caja",
      "waitingTickets": 5,
      "attendingTickets": 3,
      "completedToday": 15,
      "maxWaitTimeMinutes": 25,
      "averageServiceMinutes": 7,
      "status": "NORMAL"
    },
    {
      "queueType": "PERSONAL_BANKER",
      "displayName": "Personal Banker",
      "waitingTickets": 4,
      "attendingTickets": 2,
      "completedToday": 8,
      "maxWaitTimeMinutes": 45,
      "averageServiceMinutes": 18,
      "status": "BUSY"
    }
  ],
  "alerts": [
    {
      "type": "QUEUE_OVERLOAD",
      "message": "Cola CAJA tiene más de 10 tickets esperando",
      "severity": "HIGH",
      "timestamp": "2024-01-15T14:25:00",
      "suggestedAction": "Asignar más asesores a CAJA"
    }
  ]
}
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/admin/dashboard
```

---

## 👥 Gestión de Asesores

### Obtener Todos los Asesores

**Endpoint**: `GET /api/advisors`

**Descripción**: Lista todos los asesores del sistema con su estado actual.

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "María González",
    "email": "maria.gonzalez@banco.com",
    "status": "AVAILABLE",
    "moduleNumber": 3,
    "assignedTicketsCount": 12,
    "createdAt": "2024-01-01T08:00:00",
    "updatedAt": "2024-01-15T14:30:00"
  },
  {
    "id": 2,
    "name": "Carlos Rodríguez",
    "email": "carlos.rodriguez@banco.com",
    "status": "BUSY",
    "moduleNumber": 1,
    "assignedTicketsCount": 8,
    "createdAt": "2024-01-01T08:00:00",
    "updatedAt": "2024-01-15T14:15:00"
  },
  {
    "id": 3,
    "name": "Ana Martínez",
    "email": "ana.martinez@banco.com",
    "status": "OFFLINE",
    "moduleNumber": 5,
    "assignedTicketsCount": 15,
    "createdAt": "2024-01-01T08:00:00",
    "updatedAt": "2024-01-15T12:00:00"
  }
]
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/advisors
```

---

### Obtener Asesores Disponibles

**Endpoint**: `GET /api/advisors/disponibles`

**Descripción**: Lista solo los asesores en estado AVAILABLE.

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "María González",
    "email": "maria.gonzalez@banco.com",
    "status": "AVAILABLE",
    "moduleNumber": 3,
    "assignedTicketsCount": 12,
    "createdAt": "2024-01-01T08:00:00",
    "updatedAt": "2024-01-15T14:30:00"
  }
]
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/advisors/disponibles
```

---

### Cambiar Estado de Asesor

**Endpoint**: `PUT /api/advisors/{advisorId}/estado`

**Descripción**: Modifica el estado de un asesor.

#### Parámetros
- `advisorId`: ID numérico del asesor
- `estado`: Nuevo estado (query parameter)

#### Query Parameters
- `estado`: `AVAILABLE`, `BUSY`, `OFFLINE`

#### Response (200 OK)
```
Status: 200 OK
Body: (vacío)
```

#### Ejemplo cURL
```bash
curl -X PUT "http://localhost:8090/api/advisors/1/estado?estado=OFFLINE"
```

---

## 🚶 Estado de Colas

### Obtener Estado de Todas las Colas

**Endpoint**: `GET /api/queues`

**Descripción**: Obtiene el estado actual de todas las colas del sistema.

#### Response (200 OK)
```json
[
  {
    "queueType": "CAJA",
    "displayName": "Caja",
    "averageWaitMinutes": 5,
    "priority": 1,
    "prefix": "C",
    "totalTickets": 8,
    "waitingTickets": 5,
    "attendingTickets": 3,
    "estimatedWaitTime": 25,
    "lastUpdated": "2024-01-15T14:30:00",
    "waitingList": [
      {
        "numero": "C001",
        "positionInQueue": 1,
        "estimatedWaitMinutes": 5,
        "createdAt": "2024-01-15T14:25:00"
      },
      {
        "numero": "C002",
        "positionInQueue": 2,
        "estimatedWaitMinutes": 10,
        "createdAt": "2024-01-15T14:27:00"
      }
    ]
  },
  {
    "queueType": "PERSONAL_BANKER",
    "displayName": "Personal Banker",
    "averageWaitMinutes": 15,
    "priority": 2,
    "prefix": "P",
    "totalTickets": 6,
    "waitingTickets": 4,
    "attendingTickets": 2,
    "estimatedWaitTime": 60,
    "lastUpdated": "2024-01-15T14:30:00",
    "waitingList": [
      {
        "numero": "P001",
        "positionInQueue": 1,
        "estimatedWaitMinutes": 15,
        "createdAt": "2024-01-15T14:15:00"
      }
    ]
  }
]
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/queues
```

---

### Obtener Estado de Cola Específica

**Endpoint**: `GET /api/queues/{queueType}`

**Descripción**: Obtiene el estado de una cola específica.

#### Parámetros
- `queueType`: Tipo de cola (`CAJA`, `PERSONAL_BANKER`, `EMPRESAS`, `GERENCIA`)

#### Response (200 OK)
```json
{
  "queueType": "CAJA",
  "displayName": "Caja",
  "averageWaitMinutes": 5,
  "priority": 1,
  "prefix": "C",
  "totalTickets": 8,
  "waitingTickets": 5,
  "attendingTickets": 3,
  "estimatedWaitTime": 25,
  "lastUpdated": "2024-01-15T14:30:00",
  "waitingList": [
    {
      "numero": "C001",
      "positionInQueue": 1,
      "estimatedWaitMinutes": 5,
      "createdAt": "2024-01-15T14:25:00"
    },
    {
      "numero": "C002",
      "positionInQueue": 2,
      "estimatedWaitMinutes": 10,
      "createdAt": "2024-01-15T14:27:00"
    }
  ]
}
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/queues/CAJA
```

---

## 🐛 Endpoints de Debug

### Verificar Configuración de Telegram

**Endpoint**: `GET /api/debug/telegram-config`

**Descripción**: Verifica la configuración de Telegram sin exponer credenciales.

#### Response (200 OK)
```json
{
  "botTokenConfigured": true,
  "chatIdConfigured": true,
  "botTokenLength": 46,
  "chatId": "123456789"
}
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/debug/telegram-config
```

---

### Probar Notificación de Telegram

**Endpoint**: `GET /api/debug/test-notification`

**Descripción**: Envía una notificación de prueba por Telegram.

#### Response (200 OK) - Éxito
```json
{
  "success": true,
  "messageId": "1234",
  "texto": "🎫 NUEVO TICKET CREADO\n\nNúmero: DEBUG001\nPosición: 1\nTiempo estimado: 5 minutos\n\nCreado: 15/01/2024 14:30"
}
```

#### Response (200 OK) - Error
```json
{
  "success": false,
  "error": "Bot token inválido"
}
```

#### Ejemplo cURL
```bash
curl -X GET http://localhost:8090/api/debug/test-notification
```

---

## 📋 Códigos de Estado HTTP

### Códigos de Éxito

| Código | Descripción | Uso |
|--------|-------------|-----|
| **200 OK** | Solicitud exitosa | GET, PUT exitosos |
| **201 Created** | Recurso creado | POST exitoso (crear ticket) |

### Códigos de Error del Cliente

| Código | Descripción | Causas Comunes |
|--------|-------------|----------------|
| **400 Bad Request** | Solicitud inválida | Validación fallida, parámetros inválidos |
| **404 Not Found** | Recurso no encontrado | Ticket/asesor no existe |

### Códigos de Error del Servidor

| Código | Descripción | Causas Comunes |
|--------|-------------|----------------|
| **500 Internal Server Error** | Error interno | Error de base de datos, excepción no controlada |

### Ejemplos de Respuestas de Error

#### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El RUT/ID es obligatorio",
  "path": "/api/tickets"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-01-15T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket no encontrado",
  "path": "/api/tickets/C999"
}
```

---

## 📊 Modelos de Datos

### TicketResponse
```json
{
  "codigoReferencia": "UUID",
  "numero": "string",
  "nationalId": "string",
  "telefono": "string",
  "branchOffice": "string",
  "queueType": "CAJA|PERSONAL_BANKER|EMPRESAS|GERENCIA",
  "status": "EN_ESPERA|PROXIMO|ATENDIENDO|COMPLETADO|CANCELADO|NO_ATENDIDO",
  "positionInQueue": "integer",
  "estimatedWaitMinutes": "integer",
  "assignedAdvisor": "string|null",
  "assignedModuleNumber": "integer|null",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Advisor
```json
{
  "id": "integer",
  "name": "string",
  "email": "string",
  "status": "AVAILABLE|BUSY|OFFLINE",
  "moduleNumber": "integer",
  "assignedTicketsCount": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### QueueStatusResponse
```json
{
  "queueType": "CAJA|PERSONAL_BANKER|EMPRESAS|GERENCIA",
  "displayName": "string",
  "averageWaitMinutes": "integer",
  "priority": "integer",
  "prefix": "string",
  "totalTickets": "integer",
  "waitingTickets": "integer",
  "attendingTickets": "integer",
  "estimatedWaitTime": "integer",
  "lastUpdated": "datetime",
  "waitingList": [
    {
      "numero": "string",
      "positionInQueue": "integer",
      "estimatedWaitMinutes": "integer",
      "createdAt": "datetime"
    }
  ]
}
```

---

## 💡 Ejemplos de Uso

### Flujo Completo de Atención

#### 1. Crear Ticket
```bash
curl -X POST http://localhost:8090/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "telefono": "+56987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }'
```

#### 2. Verificar Estado de Cola
```bash
curl -X GET http://localhost:8090/api/queues/CAJA
```

#### 3. Obtener Asesores Disponibles
```bash
curl -X GET http://localhost:8090/api/advisors/disponibles
```

#### 4. Llamar Ticket (usando IDs de respuestas anteriores)
```bash
curl -X PUT http://localhost:8090/api/tickets/123/llamar/1
```

#### 5. Finalizar Ticket
```bash
curl -X PUT http://localhost:8090/api/tickets/123/finalizar
```

### Monitoreo del Sistema

#### Dashboard Completo
```bash
curl -X GET http://localhost:8090/api/admin/dashboard
```

#### Estado de Todas las Colas
```bash
curl -X GET http://localhost:8090/api/queues
```

#### Tickets Activos
```bash
curl -X GET http://localhost:8090/api/tickets
```

### Gestión de Asesores

#### Cambiar Asesor a Descanso
```bash
curl -X PUT "http://localhost:8090/api/advisors/1/estado?estado=OFFLINE"
```

#### Reactivar Asesor
```bash
curl -X PUT "http://localhost:8090/api/advisors/1/estado?estado=AVAILABLE"
```

---

## 🔧 Herramientas Recomendadas

### Postman Collection
Para facilitar las pruebas, se recomienda crear una colección de Postman con todos los endpoints documentados.

### Swagger/OpenAPI
El sistema puede extenderse con Swagger para documentación interactiva:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Monitoreo
- **Actuator**: http://localhost:8090/actuator/health
- **Métricas**: http://localhost:8090/actuator/metrics
- **Info**: http://localhost:8090/actuator/info

---

**📞 Soporte**: Para problemas con la API, consultar la [Guía de Troubleshooting](TROUBLESHOOTING.md) o revisar los logs del sistema.
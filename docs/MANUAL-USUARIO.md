# 📖 Manual de Usuario - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Introducción al Sistema](#introducción-al-sistema)
2. [Acceso al Sistema](#acceso-al-sistema)
3. [Gestión de Tickets](#gestión-de-tickets)
4. [Tipos de Colas y Atención](#tipos-de-colas-y-atención)
5. [Dashboard y Métricas](#dashboard-y-métricas)
6. [Gestión de Asesores](#gestión-de-asesores)
7. [Notificaciones de Telegram](#notificaciones-de-telegram)
8. [Casos de Uso Comunes](#casos-de-uso-comunes)
9. [Preguntas Frecuentes (FAQ)](#preguntas-frecuentes-faq)

---

## 🎯 Introducción al Sistema

### ¿Qué es el Sistema Ticketero?

El Sistema Ticketero es una solución integral para la gestión de turnos y atención al cliente en sucursales bancarias. Permite:

- **Crear tickets** de atención de forma ordenada
- **Gestionar colas** por tipo de servicio
- **Asignar asesores** de manera eficiente
- **Monitorear métricas** en tiempo real
- **Recibir notificaciones** automáticas por Telegram

### Características Principales

| Característica | Descripción |
|----------------|-------------|
| **4 Tipos de Cola** | Caja, Personal Banker, Empresas, Gerencia |
| **Notificaciones Automáticas** | 3 tipos de mensajes por Telegram |
| **Dashboard en Tiempo Real** | Métricas y alertas del sistema |
| **Gestión de Asesores** | Control de disponibilidad y asignación |
| **Interfaz Múltiple** | API REST + Interfaz de consola |

---

## 🔐 Acceso al Sistema

### Opciones de Acceso

#### 1. Interfaz de Consola (Recomendada para usuarios)
```bash
# Ejecutar desde el directorio del proyecto
run-console.bat

# O usando Maven
mvn exec:java -Dexec.mainClass="com.example.ticketero.cli.TicketeroConsoleApp"
```

#### 2. API REST (Para desarrolladores/integraciones)
- **URL Base**: http://localhost:8090
- **Documentación**: Ver [API-ENDPOINTS.md](API-ENDPOINTS.md)

### Verificar Conexión

Al iniciar la interfaz de consola, verás:
```
🎫 SISTEMA TICKETERO - INTERFAZ DE CONSOLA
==========================================

📋 MENÚ PRINCIPAL:
1. 🆕 Crear Ticket
2. 📋 Listar Tickets Activos
...
```

---

## 🎫 Gestión de Tickets

### Crear un Nuevo Ticket

#### Paso a Paso:

1. **Seleccionar opción 1** en el menú principal
2. **Ingresar datos del cliente:**
   - **RUT/Cédula**: Identificación del cliente (obligatorio)
   - **Teléfono**: Número de contacto (opcional, formato: +56XXXXXXXXX)
   - **Sucursal**: Nombre de la sucursal (obligatorio)
   - **Tipo de Cola**: Seleccionar del 1 al 4

#### Ejemplo de Creación:
```
🆕 CREAR NUEVO TICKET
====================
RUT/Cédula: 12345678-9
Teléfono (opcional): +56987654321
Sucursal: Sucursal Centro
Tipos de Cola:
1. CAJA
2. PERSONAL_BANKER
3. EMPRESAS
4. GERENCIA
Selecciona tipo de cola (1-4): 1
```

#### Respuesta del Sistema:
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "numero": "C001",
  "nationalId": "12345678-9",
  "telefono": "+56987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA",
  "status": "WAITING",
  "positionInQueue": 3,
  "estimatedWaitMinutes": 15,
  "createdAt": "2024-01-15T10:30:00"
}
```

### Consultar Tickets

#### Listar Todos los Tickets Activos
- **Opción 2** del menú principal
- Muestra todos los tickets en estado `WAITING` o `BEING_ATTENDED`

#### Buscar Ticket Específico
- **Opción 3** del menú principal
- Ingresar número de ticket (ej: C001, P015, E003, G001)

### Estados de Tickets

| Estado | Descripción | Acción Siguiente |
|--------|-------------|------------------|
| **WAITING** | En cola esperando | Llamar ticket |
| **BEING_ATTENDED** | Siendo atendido | Finalizar ticket |
| **COMPLETED** | Atención completada | - |

---

## 🏢 Tipos de Colas y Atención

### Colas Disponibles

#### 1. 💰 CAJA
- **Prefijo**: C (C001, C002, ...)
- **Tiempo Promedio**: 5 minutos
- **Prioridad**: Alta (1)
- **Servicios**: Depósitos, retiros, pagos básicos

#### 2. 👤 PERSONAL BANKER
- **Prefijo**: P (P001, P002, ...)
- **Tiempo Promedio**: 15 minutos
- **Prioridad**: Media (2)
- **Servicios**: Productos bancarios, inversiones

#### 3. 🏢 EMPRESAS
- **Prefijo**: E (E001, E002, ...)
- **Tiempo Promedio**: 20 minutos
- **Prioridad**: Media-Baja (3)
- **Servicios**: Servicios corporativos, créditos empresariales

#### 4. 👔 GERENCIA
- **Prefijo**: G (G001, G002, ...)
- **Tiempo Promedio**: 30 minutos
- **Prioridad**: Baja (4)
- **Servicios**: Casos especiales, reclamos, productos premium

### Cálculo de Tiempo de Espera

El sistema calcula automáticamente:
```
Tiempo Estimado = (Posición en Cola - 1) × Tiempo Promedio del Servicio
```

**Ejemplo**: Si eres el 4° en cola de CAJA:
```
Tiempo Estimado = (4 - 1) × 5 minutos = 15 minutos
```

---

## 📊 Dashboard y Métricas

### Acceder al Dashboard
- **Opción 6** del menú principal
- Muestra métricas en tiempo real del sistema

### Información Disponible

#### 📈 Resumen General
- **Tickets del día**: Total creados hoy
- **En espera**: Tickets esperando atención
- **En atención**: Tickets siendo atendidos
- **Completados**: Tickets finalizados hoy
- **Tiempo promedio**: Tiempo de espera promedio
- **Hora pico**: Hora de mayor demanda

#### 👥 Estado de Asesores
- **Disponibles**: Asesores listos para atender
- **Ocupados**: Asesores atendiendo clientes
- **Desconectados**: Asesores no disponibles
- **Capacidad total**: Total de asesores del sistema

#### 🚶 Estado por Cola
Para cada tipo de cola:
- Tickets esperando
- Tickets en atención
- Completados hoy
- Tiempo máximo de espera
- Tiempo promedio de servicio
- Estado general de la cola

#### ⚠️ Alertas del Sistema
- **Colas saturadas**: Más de 10 tickets esperando
- **Asesores insuficientes**: Pocos asesores disponibles
- **Tiempos elevados**: Esperas superiores a 30 minutos

### Ejemplo de Dashboard
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
  }
}
```

---

## 👥 Gestión de Asesores

### Consultar Asesores
- **Opción 7** del menú principal
- Muestra todos los asesores y su estado actual

### Estados de Asesores

| Estado | Descripción | Puede Atender |
|--------|-------------|---------------|
| **AVAILABLE** | Disponible para atender | ✅ Sí |
| **BUSY** | Atendiendo cliente | ❌ No |
| **OFFLINE** | No disponible | ❌ No |

### Asignación Automática

El sistema asigna tickets automáticamente al asesor:
1. **Disponible** (estado AVAILABLE)
2. **Con menos tickets** asignados en el día
3. **Especializado** en el tipo de cola (si aplica)

### Operaciones con Tickets

#### Llamar Ticket (Opción 4)
```
📞 LLAMAR TICKET
================
ID del ticket: 123
ID del asesor: 5
✅ Ticket llamado exitosamente!
```

**Resultado**: 
- Ticket cambia a estado `BEING_ATTENDED`
- Asesor cambia a estado `BUSY`
- Se envía notificación "Es tu turno" por Telegram

#### Finalizar Ticket (Opción 5)
```
✅ FINALIZAR TICKET
===================
ID del ticket: 123
✅ Ticket finalizado exitosamente!
```

**Resultado**:
- Ticket cambia a estado `COMPLETED`
- Asesor vuelve a estado `AVAILABLE`
- Se actualiza contador de tickets del asesor

---

## 📱 Notificaciones de Telegram

### Configuración Requerida

Para recibir notificaciones, el sistema debe tener configurado:
- **TELEGRAM_BOT_TOKEN**: Token del bot
- **TELEGRAM_CHAT_ID**: ID del chat de destino

### Tipos de Notificaciones

#### 1. 🎫 Ticket Creado
**Cuándo se envía**: Inmediatamente al crear un ticket
```
🎫 NUEVO TICKET CREADO

Número: C001
Cliente: 12345678-9
Sucursal: Sucursal Centro
Cola: Caja
Posición: 3
Tiempo estimado: 15 minutos

Creado: 15/01/2024 10:30
```

#### 2. ⏰ Próximo Turno
**Cuándo se envía**: Cuando quedan 2 personas adelante
```
⏰ PRÓXIMO TURNO

Tu ticket C001 será llamado pronto.
Quedan 2 personas adelante.
Tiempo estimado: 10 minutos

¡Mantente atento!
```

#### 3. 🔔 Es Tu Turno
**Cuándo se envía**: Al llamar el ticket
```
🔔 ES TU TURNO

Ticket: C001
Dirígete al módulo: 3
Asesor: María González

¡Tu turno ha llegado!
```

### Configuración del Bot

#### Crear Bot de Telegram:
1. Buscar **@BotFather** en Telegram
2. Enviar `/newbot`
3. Seguir instrucciones
4. Copiar el token generado

#### Obtener Chat ID:
1. Buscar **@userinfobot** en Telegram
2. Enviar `/start`
3. Copiar el ID numérico

---

## 💼 Casos de Uso Comunes

### Caso 1: Cliente Nuevo en Caja

**Escenario**: Cliente llega para hacer un depósito

1. **Crear ticket**:
   - RUT: 12345678-9
   - Teléfono: +56987654321
   - Sucursal: Centro
   - Cola: CAJA

2. **Cliente recibe**:
   - Número: C001
   - Posición: 3
   - Tiempo estimado: 15 min
   - Notificación Telegram

3. **Cuando es su turno**:
   - Asesor llama ticket
   - Cliente recibe notificación "Es tu turno"
   - Se dirige al módulo asignado

4. **Al finalizar**:
   - Asesor finaliza ticket
   - Ticket queda como completado

### Caso 2: Consulta Personal Banker

**Escenario**: Cliente quiere información sobre inversiones

1. **Crear ticket** con cola PERSONAL_BANKER
2. **Tiempo estimado**: 30 minutos (2 personas × 15 min)
3. **Notificación "próximo turno"** cuando quede 1 persona
4. **Atención personalizada** con asesor especializado

### Caso 3: Monitoreo de Sucursal

**Escenario**: Supervisor revisa estado general

1. **Ver dashboard** (Opción 6):
   - 45 tickets del día
   - 12 en espera
   - 8 en atención
   - Hora pico: 11:00-12:00

2. **Ver estado de colas** (Opción 8):
   - CAJA: 5 esperando
   - PERSONAL_BANKER: 3 esperando
   - EMPRESAS: 2 esperando
   - GERENCIA: 1 esperando

3. **Ver asesores** (Opción 7):
   - 3 disponibles
   - 5 ocupados
   - 2 desconectados

### Caso 4: Gestión de Alertas

**Escenario**: Cola de CAJA saturada

1. **Dashboard muestra alerta**:
   ```json
   {
     "type": "QUEUE_OVERLOAD",
     "message": "Cola CAJA tiene 12 tickets esperando",
     "severity": "HIGH",
     "suggestedAction": "Asignar más asesores a CAJA"
   }
   ```

2. **Acciones recomendadas**:
   - Reasignar asesores de otras colas
   - Activar asesores en estado OFFLINE
   - Informar a clientes sobre tiempos de espera

---

## ❓ Preguntas Frecuentes (FAQ)

### Sobre Tickets

**P: ¿Puedo crear un ticket sin teléfono?**
R: Sí, el teléfono es opcional. Solo RUT, sucursal y tipo de cola son obligatorios.

**P: ¿Qué significa cada prefijo de ticket?**
R: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia.

**P: ¿Puedo cambiar el tipo de cola después de crear el ticket?**
R: No, debes crear un nuevo ticket. El sistema no permite modificar tickets existentes.

**P: ¿Cómo se calcula la posición en cola?**
R: Se basa en el orden de creación dentro de cada tipo de cola.

### Sobre Tiempos de Espera

**P: ¿Por qué mi tiempo estimado cambió?**
R: El tiempo se recalcula dinámicamente según:
- Tickets que se completan antes de lo esperado
- Nuevos tickets creados
- Disponibilidad de asesores

**P: ¿Qué pasa si un asesor se demora más de lo normal?**
R: El sistema ajusta automáticamente los tiempos estimados para los siguientes tickets.

### Sobre Notificaciones

**P: ¿Por qué no recibo notificaciones de Telegram?**
R: Verifica que:
- El bot esté configurado correctamente
- El TELEGRAM_BOT_TOKEN sea válido
- El TELEGRAM_CHAT_ID sea correcto
- El bot tenga permisos para enviar mensajes

**P: ¿Puedo desactivar las notificaciones?**
R: Las notificaciones son automáticas del sistema. No se pueden desactivar por ticket individual.

### Sobre Asesores

**P: ¿Cómo se asignan los tickets a los asesores?**
R: Automáticamente al asesor disponible con menos tickets asignados en el día.

**P: ¿Puede un asesor atender múltiples colas?**
R: Sí, los asesores pueden atender cualquier tipo de cola según disponibilidad.

**P: ¿Qué pasa si no hay asesores disponibles?**
R: El ticket queda en espera hasta que un asesor esté disponible.

### Sobre el Sistema

**P: ¿Qué pasa si se reinicia el sistema?**
R: Los tickets en base de datos se mantienen. Solo se pierden los datos en memoria.

**P: ¿Puedo usar el sistema desde múltiples computadores?**
R: Sí, múltiples instancias de la interfaz de consola pueden conectarse al mismo servidor.

**P: ¿Hay límite de tickets por día?**
R: No hay límite técnico, pero el rendimiento puede verse afectado con volúmenes muy altos.

### Solución de Problemas

**P: Error "Connection refused"**
R: Verifica que:
- El servidor esté ejecutándose (puerto 8090)
- No haya firewall bloqueando la conexión
- La URL base sea correcta

**P: Error al crear ticket**
R: Revisa que:
- El RUT tenga formato válido
- El teléfono tenga formato +56XXXXXXXXX (si se proporciona)
- La sucursal no esté vacía
- El tipo de cola sea válido (1-4)

**P: Dashboard no muestra datos**
R: Puede deberse a:
- No hay tickets creados hoy
- Error de conexión con base de datos
- Problema con el servicio de dashboard

---

## 📞 Soporte y Contacto

### Recursos Adicionales

- **Guía de Despliegue**: [DESPLIEGUE-LOCAL.md](DESPLIEGUE-LOCAL.md)
- **Documentación API**: [API-ENDPOINTS.md](API-ENDPOINTS.md)
- **Solución de Problemas**: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

### Reportar Problemas

Para reportar problemas o solicitar nuevas funcionalidades:

1. **Revisar logs** del sistema
2. **Consultar troubleshooting** guide
3. **Documentar** el problema con pasos para reproducir
4. **Incluir** información del entorno (OS, Java version, etc.)

---

**📝 Nota**: Este manual cubre las funcionalidades principales del sistema. Para casos específicos o configuraciones avanzadas, consultar la documentación técnica adicional.
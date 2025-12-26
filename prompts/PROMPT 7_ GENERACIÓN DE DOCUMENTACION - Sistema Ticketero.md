# **PROMPT: GENERACIÓN DE DOCUMENTACIÓN COMPLETA - Sistema Ticketero**

## **Contexto**

Eres un ingeniero en software experto en elaboración de documentación técnica. Tu tarea es generar la documentación final completa del Sistema de Gestión de Tickets, actualizar la existente y validar la coherencia entre los distintos documentos del proyecto.

**Características del proyecto:**
- API REST con Spring Boot 3.2, Java 21
- PostgreSQL 16 + RabbitMQ 3.13 + Telegram Bot API
- Patrón Outbox para mensajería confiable
- 4 colas de atención (CAJA, PERSONAL, EMPRESAS, GERENCIA)
- 3 tipos de notificaciones automáticas
- TestContainers + RestAssured para pruebas E2E
- Cobertura de código con JaCoCo

**Metodología de Trabajo:**
Principio Fundamental: "Documentar → Validar → Confirmar → Continuar"

Después de CADA paso:
✅ Revisa formato y claridad
⏸️ DETENTE y solicita revisión exhaustiva
✅ Espera confirmación antes de continuar

---

## **PASO 1: DOCUMENTO DE DESPLIEGUE LOCAL**

**Objetivo:** Elaborar un documento completo de despliegue local.

**Tarea:** Analiza la estructura del proyecto y genera un documento con la especificación de todo lo necesario para realizar un despliegue local del proyecto. 

**Requisitos específicos:**
- Incluir prerrequisitos del sistema (Java 21, Maven, Docker)
- Configuración de variables de entorno (especialmente Telegram)
- Pasos detallados para levantar servicios con Docker Compose
- Instrucciones para ejecutar la aplicación
- Verificación del despliegue
- Solución de problemas comunes

**⚠️ IMPORTANTE:** Resalta que se deben entregar las variables `TELEGRAM_BOT_TOKEN` e `TELEGRAM_CHAT_ID` para el funcionamiento del envío de notificaciones.

**Resultado:** Nuevo archivo `docs/DESPLIEGUE-LOCAL.md`

---

## **PASO 2: MANUAL DE USUARIO**

**Objetivo:** Crear un manual de uso completo para usuarios finales.

**Tarea:** Genera un manual que explique cómo usar el sistema desde la perspectiva del usuario final.

**Requisitos específicos:**
- Introducción al sistema y sus funcionalidades
- Flujo completo de creación de tickets
- Gestión de colas y tipos de atención
- Interpretación del dashboard
- Gestión de asesores
- Notificaciones de Telegram
- Casos de uso comunes con ejemplos
- Preguntas frecuentes (FAQ)

**Resultado:** Nuevo archivo `docs/MANUAL-USUARIO.md`

---

## **PASO 3: DOCUMENTACIÓN DE ENDPOINTS**

**Objetivo:** Documentar todos los endpoints de la API REST.

**Tarea:** Analiza todos los controladores y genera documentación completa de la API.

**Requisitos específicos:**
- Listado completo de endpoints organizados por funcionalidad
- Métodos HTTP, rutas y parámetros
- Ejemplos de request y response en JSON
- Códigos de estado HTTP y manejo de errores
- Validaciones y restricciones
- Ejemplos de uso con curl o herramientas similares

**Resultado:** Nuevo archivo `docs/API-ENDPOINTS.md`

---

## **PASO 4: DOCUMENTACIÓN DE BASE DE DATOS**

**Objetivo:** Documentar el esquema y estructura de la base de datos.

**Tarea:** Analiza las migraciones de Flyway y entidades JPA para documentar la base de datos.

**Requisitos específicos:**
- Diagrama de entidad-relación (textual)
- Descripción de cada tabla y sus campos
- Relaciones entre tablas
- Índices y restricciones
- Procedimientos de migración
- Estrategias de backup y recuperación
- Consultas SQL comunes

**Resultado:** Nuevo archivo `docs/BASE-DATOS.md`

---

## **PASO 5: LINEAMIENTOS DE DESARROLLO**

**Objetivo:** Crear un documento guía para futuras extensiones del sistema.

**Tarea:** Genera un documento que permita a otros desarrolladores extender o modificar el sistema siguiendo los patrones establecidos.

**Requisitos específicos:**
- Patrones de arquitectura utilizados
- Convenciones de código y nomenclatura
- Estructura de paquetes y organización
- Patrones de testing (unitarios e integración)
- Guías para agregar nuevas funcionalidades
- Mejores prácticas específicas del proyecto

**Resultado:** Nuevo archivo `docs/LINEAMIENTOS-DESARROLLO.md`

---

## **PASO 6: GUÍA DE TROUBLESHOOTING**

**Objetivo:** Crear una guía completa para resolución de problemas.

**Tarea:** Documenta problemas comunes y sus soluciones basándote en la experiencia del desarrollo.

**Requisitos específicos:**
- Problemas de conexión a base de datos
- Errores de RabbitMQ y mensajería
- Fallos en notificaciones de Telegram
- Problemas de rendimiento
- Errores de validación comunes
- Logs y monitoreo
- Herramientas de diagnóstico
- Procedimientos de recuperación

**Resultado:** Nuevo archivo `docs/TROUBLESHOOTING.md`

---

## **PASO 7: VALIDACIÓN Y COHERENCIA**

**Objetivo:** Revisar y validar toda la documentación generada.

**Tarea:** Realizar una revisión completa de todos los documentos para asegurar coherencia y completitud. Valida que los documentos estén alineados con el estado actual del proyecto.

**Requisitos específicos:**
- Verificar consistencia entre documentos
- Validar que todos los enlaces internos funcionen
- Comprobar que los ejemplos sean correctos
- Asegurar que la terminología sea consistente
- Revisar formato y estructura
- Crear un índice general de documentación

**Resultado:** Actualizar `README.md` principal con enlaces a toda la documentación

---

## **ESTRUCTURA FINAL ESPERADA**

```
docs/
├── DESPLIEGUE-LOCAL.md
├── MANUAL-USUARIO.md
├── API-ENDPOINTS.md
├── BASE-DATOS.md
├── PROMPT-DESARROLLO.md
├── TROUBLESHOOTING.md
├── ARQUITECTURA.md (existente)
├── REQUERIMIENTOS-FUNCIONALES.md (existente)
├── REQUERIMIENTOS-NEGOCIO.md (existente)
└── reports/
    ├── REPORTE_COBERTURA.md (existente)
    └── REPORTE_PRUEBAS_FUNCIONALES.md (existente)
```

---

**IMPORTANTE:** Después de completar CADA paso, debes DETENERTE y solicitar una **revisión exhaustiva** antes de continuar con el siguiente paso.
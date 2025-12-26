# 🎫 Sistema Ticketero - Gestión de Turnos Bancarios

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema integral de gestión de tickets para sucursales bancarias con notificaciones en tiempo real vía Telegram. Implementa patrones de arquitectura empresarial con Spring Boot 3.2 y Java 21.

## 🚀 Características Principales

- **4 Tipos de Colas**: CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA
- **Notificaciones Automáticas**: 3 tipos de mensajes por Telegram
- **Patrón Outbox**: Mensajería confiable con RabbitMQ
- **Dashboard en Tiempo Real**: Métricas y alertas del sistema
- **Recuperación Automática**: Sistema de recuperación de fallos
- **Cobertura de Código**: JaCoCo con TestContainers

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │    Services     │    │  Repositories   │
│   (REST API)    │───▶│ (Lógica Negocio)│───▶│ (Acceso Datos)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  CLI Interface  │    │   Schedulers    │    │   PostgreSQL    │
│   (Consola)     │    │ (Notificaciones)│    │   (Base Datos)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │    RabbitMQ     │
                       │   (Mensajería)  │
                       └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Telegram API   │
                       │ (Notificaciones)│
                       └─────────────────┘
```

## 📚 Documentación Completa

### 📖 Documentación de Usuario
- **[Manual de Usuario](docs/MANUAL-USUARIO.md)** - Guía completa para usuarios finales
- **[Guía de Despliegue Local](docs/DESPLIEGUE-LOCAL.md)** - Instrucciones de instalación y configuración

### 🔧 Documentación Técnica
- **[Arquitectura del Sistema](docs/ARQUITECTURA.md)** - Diseño de alto nivel y patrones
- **[API REST Endpoints](docs/API-ENDPOINTS.md)** - Documentación completa de la API
- **[Base de Datos](docs/BASE-DATOS.md)** - Esquema, migraciones y consultas
- **[Lineamientos de Desarrollo](docs/LINEAMIENTOS-DESARROLLO.md)** - Patrones y convenciones de código

### 📋 Documentación de Negocio
- **[Requerimientos de Negocio](docs/REQUERIMIENTOS-NEGOCIO.md)** - Necesidades y objetivos del sistema
- **[Requerimientos Funcionales](docs/REQUERIMIENTOS-FUNCIONALES.md)** - Especificaciones técnicas detalladas

### 🛠️ Soporte y Mantenimiento
- **[Guía de Troubleshooting](docs/TROUBLESHOOTING.md)** - Solución de problemas comunes
- **[Reportes de Pruebas](docs/reports/)** - Cobertura de código y pruebas funcionales

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21+
- Maven 3.9+
- Docker y Docker Compose
- Variables de Telegram configuradas

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <url-del-repositorio>
   cd gestor-de-tickets
   ```

2. **Configurar variables de entorno**
   ```bash
   cp .env.example .env
   # Editar .env con tus credenciales de Telegram
   ```

3. **Levantar servicios**
   ```bash
   docker-compose up --build -d
   ```

4. **Verificar instalación**
   ```bash
   curl http://localhost:8090/actuator/health
   ```

### Uso Básico

#### Interfaz de Consola
```bash
# Ejecutar interfaz de usuario
run-console.bat

# O usando Maven
mvn exec:java -Dexec.mainClass="com.example.ticketero.cli.TicketeroConsoleApp"
```

#### API REST
```bash
# Crear ticket
curl -X POST http://localhost:8090/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "telefono": "+56987654321",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }'

# Obtener tickets activos
curl http://localhost:8090/api/tickets

# Ver dashboard
curl http://localhost:8090/api/admin/dashboard
```

## 🏢 Tipos de Colas

| Cola | Prefijo | Tiempo Promedio | Servicios |
|------|---------|-----------------|-----------|
| **CAJA** | C | 5 min | Depósitos, retiros, pagos |
| **PERSONAL_BANKER** | P | 15 min | Productos bancarios, inversiones |
| **EMPRESAS** | E | 20 min | Servicios corporativos |
| **GERENCIA** | G | 30 min | Casos especiales, reclamos |

## 📱 Notificaciones de Telegram

### Tipos de Mensajes
1. **🎫 Ticket Creado** - Confirmación inmediata
2. **⏰ Próximo Turno** - Aviso cuando quedan 2 personas
3. **🔔 Es Tu Turno** - Llamada a atención

### Configuración Requerida
```bash
# Variables obligatorias en .env
TELEGRAM_BOT_TOKEN=tu_token_del_bot
TELEGRAM_CHAT_ID=tu_chat_id
```

## 🧪 Testing

### Ejecutar Pruebas
```bash
# Pruebas unitarias
mvn test

# Pruebas de integración
mvn test -Dtest="*IT"

# Reporte de cobertura
mvn clean test jacoco:report
```

### Cobertura Actual
- **Líneas**: 85%+
- **Ramas**: 80%+
- **Métodos**: 90%+

## 📊 Monitoreo

### Endpoints de Salud
- **Health Check**: http://localhost:8090/actuator/health
- **Métricas**: http://localhost:8090/actuator/metrics
- **Info**: http://localhost:8090/actuator/info

### Interfaces de Gestión
- **RabbitMQ Management**: http://localhost:15672 (dev/dev123)
- **Base de Datos**: PostgreSQL en puerto 5432

## 🔧 Desarrollo

### Stack Tecnológico
- **Backend**: Spring Boot 3.2, Java 21
- **Base de Datos**: PostgreSQL 16 + Flyway
- **Mensajería**: RabbitMQ 3.13
- **Testing**: JUnit 5, TestContainers, RestAssured
- **Build**: Maven 3.9+

### Estructura del Proyecto
```
src/
├── main/java/com/example/ticketero/
│   ├── cli/                    # Interfaz de consola
│   ├── controller/             # Controladores REST
│   ├── service/               # Lógica de negocio
│   ├── repository/            # Acceso a datos
│   ├── model/                 # Entidades y DTOs
│   └── config/                # Configuraciones
├── main/resources/
│   ├── db/migration/          # Migraciones Flyway
│   └── application.yml        # Configuración
└── test/                      # Pruebas unitarias e integración
```

### Comandos de Desarrollo
```bash
# Compilar
mvn clean compile

# Ejecutar aplicación
mvn spring-boot:run

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generar documentación
mvn javadoc:javadoc
```

## 🚨 Troubleshooting

### Problemas Comunes

#### Error de Conexión a Base de Datos
```bash
# Verificar PostgreSQL
docker-compose logs postgres

# Reiniciar servicios
docker-compose restart postgres
```

#### Notificaciones de Telegram No Funcionan
```bash
# Verificar configuración
curl http://localhost:8090/api/debug/telegram-config

# Probar notificación
curl http://localhost:8090/api/debug/test-notification
```

#### Para más problemas, consultar la [Guía de Troubleshooting](docs/TROUBLESHOOTING.md)

## 📈 Roadmap

### Versión Actual (1.0.0)
- ✅ Sistema básico de tickets
- ✅ 4 tipos de colas
- ✅ Notificaciones de Telegram
- ✅ Dashboard administrativo
- ✅ Interfaz de consola

### Próximas Versiones
- 🔄 Interfaz web (React)
- 📊 Reportes avanzados
- 🔐 Autenticación y autorización
- 📱 App móvil
- 🌐 Multi-sucursal

## 🤝 Contribución

### Cómo Contribuir
1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Lineamientos
- Seguir [Lineamientos de Desarrollo](docs/LINEAMIENTOS-DESARROLLO.md)
- Mantener cobertura de pruebas >80%
- Documentar nuevas funcionalidades
- Usar conventional commits

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👥 Equipo

### Desarrolladores
- **Arquitecto de Software** - Diseño del sistema
- **Desarrollador Backend** - Implementación Spring Boot
- **Especialista en Base de Datos** - PostgreSQL y optimización
- **DevOps Engineer** - Docker y despliegue

### Contacto
- **Email**: soporte@ticketero.com
- **Documentación**: [Wiki del Proyecto](docs/)
- **Issues**: [GitHub Issues](issues/)

## 🙏 Agradecimientos

- Spring Boot Team por el excelente framework
- PostgreSQL Community por la robusta base de datos
- RabbitMQ Team por la mensajería confiable
- Telegram Bot API por las notificaciones
- TestContainers por facilitar las pruebas de integración

---

**📞 Soporte**: Para problemas técnicos, consultar la [Guía de Troubleshooting](docs/TROUBLESHOOTING.md) o crear un issue en el repositorio.

**📚 Documentación Completa**: Toda la documentación técnica está disponible en el directorio [docs/](docs/).
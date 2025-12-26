# 🎫 Sistema Ticketero - Interfaz de Consola

## 🚀 Cómo Ejecutar

### 1. Iniciar Servicios
```bash
# Iniciar PostgreSQL y RabbitMQ
docker-compose up postgres rabbitmq -d

# Iniciar la API (en otra terminal)
mvn spring-boot:run
```

### 2. Ejecutar Interfaz de Consola
```bash
# Opción 1: Usar el script
run-console.bat

# Opción 2: Comando directo
mvn exec:java -Dexec.mainClass="com.example.ticketero.cli.TicketeroConsoleApp"
```

## 📋 Funcionalidades Disponibles

1. **🆕 Crear Ticket** - Crear nuevo ticket en el sistema
2. **📋 Listar Tickets Activos** - Ver todos los tickets en espera
3. **🔍 Buscar Ticket** - Buscar ticket por número
4. **📞 Llamar Ticket** - Asignar ticket a un asesor
5. **✅ Finalizar Ticket** - Completar atención de ticket
6. **📊 Ver Dashboard** - Métricas del sistema
7. **👥 Ver Asesores** - Estado de asesores disponibles
8. **🚶 Ver Estado de Colas** - Estado actual de todas las colas

## 🔧 Configuración

- **API URL**: http://localhost:8090
- **Puerto PostgreSQL**: 5432
- **Puerto RabbitMQ**: 5672
- **RabbitMQ Management**: http://localhost:15672 (dev/dev123)

## 📝 Ejemplo de Uso

1. Ejecutar `run-console.bat`
2. Seleccionar opción `1` para crear ticket
3. Ingresar datos del cliente
4. Seleccionar tipo de cola
5. El sistema creará el ticket y enviará notificación por Telegram

## ⚠️ Requisitos

- Java 21
- Maven 3.6+
- Docker y Docker Compose
- API ejecutándose en puerto 8090
# 🚀 Guía de Despliegue Local - Sistema Ticketero

## 📋 Tabla de Contenidos

1. [Prerrequisitos del Sistema](#prerrequisitos-del-sistema)
2. [Configuración de Variables de Entorno](#configuración-de-variables-de-entorno)
3. [Despliegue con Docker Compose](#despliegue-con-docker-compose)
4. [Despliegue Manual](#despliegue-manual)
5. [Verificación del Despliegue](#verificación-del-despliegue)
6. [Interfaz de Consola](#interfaz-de-consola)
7. [Solución de Problemas Comunes](#solución-de-problemas-comunes)
8. [Comandos Útiles](#comandos-útiles)

---

## 🔧 Prerrequisitos del Sistema

### Software Requerido

| Componente | Versión Mínima | Versión Recomendada | Verificación |
|------------|----------------|---------------------|--------------|
| **Java JDK** | 21 | 21 LTS | `java -version` |
| **Maven** | 3.6+ | 3.9+ | `mvn -version` |
| **Docker** | 20.0+ | 24.0+ | `docker --version` |
| **Docker Compose** | 2.0+ | 2.20+ | `docker-compose --version` |
| **Git** | 2.30+ | 2.40+ | `git --version` |

### Instalación de Prerrequisitos

#### Windows
```powershell
# Instalar Java 21 (usando Chocolatey)
choco install openjdk21

# Instalar Maven
choco install maven

# Instalar Docker Desktop
# Descargar desde: https://www.docker.com/products/docker-desktop
```

#### Linux (Ubuntu/Debian)
```bash
# Instalar Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# Instalar Maven
sudo apt install maven

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo apt install docker-compose-plugin
```

#### macOS
```bash
# Usando Homebrew
brew install openjdk@21 maven docker docker-compose
```

---

## 🔐 Configuración de Variables de Entorno

### ⚠️ IMPORTANTE: Variables de Telegram

**El sistema requiere obligatoriamente las siguientes variables para el funcionamiento de las notificaciones:**

- `TELEGRAM_BOT_TOKEN`: Token del bot de Telegram
- `TELEGRAM_CHAT_ID`: ID del chat donde se enviarán las notificaciones

### Obtener Credenciales de Telegram

#### 1. Crear Bot de Telegram
```
1. Abrir Telegram y buscar @BotFather
2. Enviar comando: /newbot
3. Seguir instrucciones para crear el bot
4. Copiar el token proporcionado (formato: 123456789:ABCdefGHIjklMNOpqrSTUvwxYZ)
```

#### 2. Obtener Chat ID
```
1. Buscar @userinfobot en Telegram
2. Enviar comando: /start
3. Copiar el ID proporcionado (formato numérico)
```

### Configurar Variables

#### Opción 1: Archivo .env (Recomendado)
```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar archivo .env
TELEGRAM_BOT_TOKEN=tu_token_real_aqui
TELEGRAM_CHAT_ID=tu_chat_id_aqui

# Variables adicionales (opcionales)
DATABASE_URL=jdbc:postgresql://localhost:5432/ticketero
DATABASE_USERNAME=dev
DATABASE_PASSWORD=dev123
```

#### Opción 2: Variables de Sistema
```bash
# Windows (PowerShell)
$env:TELEGRAM_BOT_TOKEN="tu_token_aqui"
$env:TELEGRAM_CHAT_ID="tu_chat_id_aqui"

# Linux/macOS
export TELEGRAM_BOT_TOKEN="tu_token_aqui"
export TELEGRAM_CHAT_ID="tu_chat_id_aqui"
```

---

## 🐳 Despliegue con Docker Compose

### Despliegue Completo (Recomendado)

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd gestor-de-tickets

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales de Telegram

# 3. Construir y levantar todos los servicios
docker-compose up --build -d

# 4. Verificar que todos los servicios estén corriendo
docker-compose ps
```

### Despliegue por Servicios

```bash
# Solo base de datos y RabbitMQ
docker-compose up postgres rabbitmq -d

# Esperar a que los servicios estén listos
docker-compose logs -f postgres rabbitmq

# Ejecutar API localmente
mvn spring-boot:run
```

### Servicios Incluidos

| Servicio | Puerto | URL | Credenciales |
|----------|--------|-----|--------------|
| **API REST** | 8080 | http://localhost:8080 | - |
| **PostgreSQL** | 5432 | localhost:5432 | dev/dev123 |
| **RabbitMQ** | 5672 | localhost:5672 | dev/dev123 |
| **RabbitMQ Management** | 15672 | http://localhost:15672 | dev/dev123 |

---

## 🛠️ Despliegue Manual

### 1. Preparar Base de Datos

```bash
# Iniciar PostgreSQL
docker run -d \
  --name ticketero-db \
  -p 5432:5432 \
  -e POSTGRES_DB=ticketero \
  -e POSTGRES_USER=dev \
  -e POSTGRES_PASSWORD=dev123 \
  postgres:16-alpine
```

### 2. Preparar RabbitMQ

```bash
# Iniciar RabbitMQ
docker run -d \
  --name ticketero-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=dev \
  -e RABBITMQ_DEFAULT_PASS=dev123 \
  rabbitmq:3.12-management-alpine
```

### 3. Compilar y Ejecutar API

```bash
# Compilar proyecto
mvn clean compile

# Ejecutar migraciones de base de datos
mvn flyway:migrate

# Ejecutar pruebas (opcional)
mvn test

# Iniciar aplicación
mvn spring-boot:run
```

### 4. Configuración Alternativa de Puerto

Si el puerto 8080 está ocupado, modificar `application.yml`:

```yaml
server:
  port: 8090  # Cambiar puerto
```

---

## ✅ Verificación del Despliegue

### 1. Verificar Servicios Docker

```bash
# Ver estado de contenedores
docker-compose ps

# Ver logs de servicios
docker-compose logs api
docker-compose logs postgres
docker-compose logs rabbitmq
```

### 2. Verificar API REST

```bash
# Health check
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

### 3. Verificar Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it ticketero-db psql -U dev -d ticketero

# Verificar tablas
\dt

# Salir
\q
```

### 4. Verificar RabbitMQ

- Abrir navegador: http://localhost:15672
- Usuario: `dev`
- Contraseña: `dev123`
- Verificar que aparezcan las colas del sistema

### 5. Verificar Notificaciones de Telegram

```bash
# Crear un ticket de prueba
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678",
    "telefono": "555-1234",
    "branchOffice": "Sucursal Centro",
    "queueType": "CAJA"
  }'
```

**Resultado esperado:** Mensaje de confirmación en el chat de Telegram configurado.

---

## 🖥️ Interfaz de Consola

### Ejecutar Interfaz de Usuario

```bash
# Opción 1: Script de Windows
run-console.bat

# Opción 2: Comando Maven
mvn exec:java -Dexec.mainClass="com.example.ticketero.cli.TicketeroConsoleApp"
```

### Funcionalidades Disponibles

1. **🆕 Crear Ticket** - Crear nuevo ticket
2. **📋 Listar Tickets Activos** - Ver tickets en espera
3. **🔍 Buscar Ticket** - Buscar por número
4. **📞 Llamar Ticket** - Asignar a asesor
5. **✅ Finalizar Ticket** - Completar atención
6. **📊 Ver Dashboard** - Métricas del sistema
7. **👥 Ver Asesores** - Estado de asesores
8. **🚶 Ver Estado de Colas** - Estado de colas

---

## 🚨 Solución de Problemas Comunes

### Error: "Port already in use"

```bash
# Verificar qué proceso usa el puerto
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/macOS

# Cambiar puerto en application.yml o detener proceso
```

### Error: "Connection refused" (PostgreSQL)

```bash
# Verificar que PostgreSQL esté corriendo
docker ps | grep postgres

# Reiniciar contenedor
docker-compose restart postgres

# Verificar logs
docker-compose logs postgres
```

### Error: "RabbitMQ connection failed"

```bash
# Verificar estado de RabbitMQ
docker-compose logs rabbitmq

# Reiniciar servicio
docker-compose restart rabbitmq

# Verificar conectividad
telnet localhost 5672
```

### Error: "Telegram notifications not working"

1. **Verificar variables de entorno:**
   ```bash
   echo $TELEGRAM_BOT_TOKEN
   echo $TELEGRAM_CHAT_ID
   ```

2. **Verificar bot de Telegram:**
   - Enviar mensaje manual al bot
   - Verificar que el bot esté activo

3. **Verificar logs de la aplicación:**
   ```bash
   docker-compose logs api | grep -i telegram
   ```

### Error: "Flyway migration failed"

```bash
# Limpiar base de datos y reiniciar
docker-compose down -v
docker-compose up postgres -d
mvn flyway:clean flyway:migrate
```

### Error: "Tests failing"

```bash
# Ejecutar tests con más información
mvn test -X

# Ejecutar solo tests unitarios
mvn test -Dtest="*Test"

# Saltar tests durante compilación
mvn clean package -DskipTests
```

---

## 📝 Comandos Útiles

### Docker Compose

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar servicio específico
docker-compose restart api

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reconstruir imágenes
docker-compose build --no-cache
```

### Maven

```bash
# Compilar sin tests
mvn clean compile -DskipTests

# Ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generar reporte de cobertura
mvn clean test jacoco:report

# Ejecutar solo tests de integración
mvn test -Dtest="*IT"
```

### Base de Datos

```bash
# Backup de base de datos
docker exec ticketero-db pg_dump -U dev ticketero > backup.sql

# Restaurar backup
docker exec -i ticketero-db psql -U dev ticketero < backup.sql

# Conectar a base de datos
docker exec -it ticketero-db psql -U dev -d ticketero
```

---

## 📊 Monitoreo y Logs

### Endpoints de Monitoreo

- **Health Check**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/metrics
- **Info**: http://localhost:8080/actuator/info

### Ubicación de Logs

```bash
# Logs de aplicación
docker-compose logs api

# Logs de base de datos
docker-compose logs postgres

# Logs de RabbitMQ
docker-compose logs rabbitmq
```

---

## 🎯 Próximos Pasos

Una vez completado el despliegue local:

1. **Probar funcionalidades básicas** usando la interfaz de consola
2. **Verificar notificaciones** de Telegram
3. **Revisar dashboard** de métricas
4. **Consultar documentación** de API endpoints
5. **Ejecutar suite de pruebas** completa

---

**📞 Soporte**: Para problemas adicionales, consultar la [Guía de Troubleshooting](TROUBLESHOOTING.md) o revisar los logs detallados del sistema.
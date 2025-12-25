# **PROMPT 6B: PRUEBAS FUNCIONALES E2E \- Testing de Integración del Sistema Ticketero**

## **Contexto**

Eres un QA Engineer Senior experto en testing E2E. Tu tarea es crear **pruebas funcionales de integración** con TestContainers \+ RestAssured para el sistema Ticketero, validando flujos completos de negocio.

**Características del proyecto:**

* API REST con Spring Boot 3.2, Java 21  
* PostgreSQL 16 \+ RabbitMQ 3.13 \+ Telegram Bot API  
* Patrón Outbox para mensajería confiable  
* 4 colas de atención (CAJA, PERSONAL, EMPRESAS, GERENCIA)  
* 3 tipos de notificaciones automáticas

**IMPORTANTE:** Después de completar CADA paso, debes DETENERTE y solicitar una **revisión exhaustiva** antes de continuar.

---

## **Documentos de Entrada**

**Lee estos archivos del proyecto:**

1. `src/main/java/com/example/ticketero/controller/` \- Endpoints a testear  
2. `src/main/java/com/example/ticketero/model/dto/` \- Request/Response DTOs  
3. `docker-compose.yml` \- Servicios de infraestructura  
4. `docs/ARCHITECTURE.md` \- Flujos de negocio

---

## **Metodología de Trabajo**

### **Principio:**

**"Diseñar → Implementar → Ejecutar → Confirmar → Continuar"**

Después de CADA paso:

1. ✅ Diseña los escenarios de prueba (Gherkin)  
2. ✅ Implementa los tests con TestContainers  
3. ✅ Ejecuta `mvn test -Dtest=NombreIT`  
4. ⏸️ **DETENTE y solicita revisión**  
5. ✅ Espera confirmación antes de continuar

### **Formato de Solicitud de Revisión:**

✅ PASO X COMPLETADO

Escenarios implementados:  
\- \[Escenario 1\]  
\- \[Escenario 2\]  
\- ...

Validaciones:  
\- HTTP: ✅  
\- Base de datos: ✅  
\- RabbitMQ: ✅  
\- Telegram: ✅ (mock)

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

1\. ¿Los escenarios cubren el flujo de negocio?  
2\. ¿Las validaciones son suficientes?  
3\. ¿Puedo continuar con el siguiente paso?

⏸️ ESPERANDO CONFIRMACIÓN...

---

## **Stack de Testing E2E**

| Componente | Versión | Propósito |
| ----- | ----- | ----- |
| JUnit 5 (Jupiter) | 5.10+ | Framework de testing |
| TestContainers | 1.19+ | PostgreSQL \+ RabbitMQ reales |
| RestAssured | 5.4+ | Testing de APIs REST |
| WireMock | 3.0+ | Mock de Telegram API |
| Awaitility | 4.2+ | Esperas asíncronas |

**Diferencia con Unit Tests:**

* ✅ `@SpringBootTest` con contexto completo  
* ✅ Base de datos real (TestContainers)  
* ✅ RabbitMQ real (TestContainers)  
* ✅ Telegram mockeado (WireMock)

---

## **Tu Tarea: 7 Pasos**

**PASO 1:** Setup TestContainers \+ Base de Tests  
**PASO 2:** Feature: Creación de Tickets (6 escenarios)  
**PASO 3:** Feature: Procesamiento de Tickets (5 escenarios)  
**PASO 4:** Feature: Notificaciones Telegram (4 escenarios)  
**PASO 5:** Feature: Validaciones de Input (5 escenarios)  
**PASO 6:** Feature: Dashboard Admin (4 escenarios)  
**PASO 7:** Ejecución Final y Reporte

**Total:** \~24 escenarios E2E | Cobertura de flujos: 100%

---

## **Estructura de Archivos a Crear**

src/test/java/com/example/ticketero/  
├── integration/  
│   ├── BaseIntegrationTest.java  
│   ├── TicketCreationIT.java  
│   ├── TicketProcessingIT.java  
│   ├── NotificationIT.java  
│   ├── ValidationIT.java  
│   └── AdminDashboardIT.java  
└── config/  
    └── WireMockConfig.java

---

## **PASO 1: Setup TestContainers \+ Base de Tests**

**Objetivo:** Configurar infraestructura de testing E2E.

### **1.1 BaseIntegrationTest.java**

package com.example.ticketero.integration;

import io.restassured.RestAssured;  
import io.restassured.http.ContentType;  
import org.junit.jupiter.api.BeforeAll;  
import org.junit.jupiter.api.BeforeEach;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.boot.test.context.SpringBootTest;  
import org.springframework.boot.test.web.server.LocalServerPort;  
import org.springframework.jdbc.core.JdbcTemplate;  
import org.springframework.test.context.ActiveProfiles;  
import org.springframework.test.context.DynamicPropertyRegistry;  
import org.springframework.test.context.DynamicPropertySource;  
import org.testcontainers.containers.PostgreSQLContainer;  
import org.testcontainers.containers.RabbitMQContainer;  
import org.testcontainers.junit.jupiter.Container;  
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

/\*\*  
 \* Base class for all integration tests.  
 \* Provides TestContainers setup and common utilities.  
 \*/  
@SpringBootTest(webEnvironment \= SpringBootTest.WebEnvironment.RANDOM\_PORT)  
@Testcontainers  
@ActiveProfiles("test")  
public abstract class BaseIntegrationTest {

    @LocalServerPort  
    protected int port;

    @Autowired  
    protected JdbcTemplate jdbcTemplate;

    // \============================================================  
    // TESTCONTAINERS  
    // \============================================================

    @Container  
    static PostgreSQLContainer\<?\> postgres \= new PostgreSQLContainer\<\>("postgres:16-alpine")  
        .withDatabaseName("ticketero\_test")  
        .withUsername("test")  
        .withPassword("test");

    @Container  
    static RabbitMQContainer rabbitmq \= new RabbitMQContainer("rabbitmq:3.13-management-alpine")  
        .withExposedPorts(5672, 15672);

    @DynamicPropertySource  
    static void configureProperties(DynamicPropertyRegistry registry) {  
        // PostgreSQL  
        registry.add("spring.datasource.url", postgres::getJdbcUrl);  
        registry.add("spring.datasource.username", postgres::getUsername);  
        registry.add("spring.datasource.password", postgres::getPassword);

        // RabbitMQ  
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);  
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);  
        registry.add("spring.rabbitmq.username", () \-\> "guest");  
        registry.add("spring.rabbitmq.password", () \-\> "guest");

        // Telegram Mock (WireMock)  
        registry.add("telegram.api-url", () \-\> "http://localhost:8089/bot");  
        registry.add("telegram.bot-token", () \-\> "test-token");  
        registry.add("telegram.chat-id", () \-\> "123456789");  
    }

    // \============================================================  
    // SETUP  
    // \============================================================

    @BeforeAll  
    static void setupContainers() {  
        postgres.start();  
        rabbitmq.start();  
    }

    @BeforeEach  
    void setupRestAssured() {  
        RestAssured.port \= port;  
        RestAssured.basePath \= "/api";  
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();  
    }

    @BeforeEach  
    void cleanDatabase() {  
        // Limpiar en orden correcto (FK constraints)  
        jdbcTemplate.execute("DELETE FROM ticket\_event");  
        jdbcTemplate.execute("DELETE FROM recovery\_event");  
        jdbcTemplate.execute("DELETE FROM outbox\_message");  
        jdbcTemplate.execute("DELETE FROM ticket");  
        jdbcTemplate.execute("UPDATE advisor SET status \= 'AVAILABLE', total\_tickets\_served \= 0");  
    }

    // \============================================================  
    // UTILITIES  
    // \============================================================

    protected String createTicketRequest(String nationalId, String telefono,   
                                          String branchOffice, String queueType) {  
        return String.format("""  
            {  
                "nationalId": "%s",  
                "telefono": "%s",  
                "branchOffice": "%s",  
                "queueType": "%s"  
            }  
            """, nationalId, telefono, branchOffice, queueType);  
    }

    protected String createTicketRequest(String nationalId, String queueType) {  
        return createTicketRequest(nationalId, "+56912345678", "Sucursal Centro", queueType);  
    }

    protected int countTicketsInStatus(String status) {  
        return jdbcTemplate.queryForObject(  
            "SELECT COUNT(\*) FROM ticket WHERE status \= ?",  
            Integer.class, status);  
    }

    protected int countOutboxMessages(String status) {  
        return jdbcTemplate.queryForObject(  
            "SELECT COUNT(\*) FROM outbox\_message WHERE status \= ?",  
            Integer.class, status);  
    }

    protected int countAdvisorsInStatus(String status) {  
        return jdbcTemplate.queryForObject(  
            "SELECT COUNT(\*) FROM advisor WHERE status \= ?",  
            Integer.class, status);  
    }

    protected void waitForTicketProcessing(int expectedCompleted, int timeoutSeconds) {  
        org.awaitility.Awaitility.await()  
            .atMost(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)  
            .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)  
            .until(() \-\> countTicketsInStatus("COMPLETED") \>= expectedCompleted);  
    }

    protected void setAdvisorStatus(Long advisorId, String status) {  
        jdbcTemplate.update(  
            "UPDATE advisor SET status \= ? WHERE id \= ?",  
            status, advisorId);  
    }  
}

### **1.2 WireMockConfig.java**

package com.example.ticketero.config;

import com.github.tomakehurst.wiremock.WireMockServer;  
import com.github.tomakehurst.wiremock.client.WireMock;  
import org.springframework.boot.test.context.TestConfiguration;  
import org.springframework.context.annotation.Bean;

import static com.github.tomakehurst.wiremock.client.WireMock.\*;

/\*\*  
 \* WireMock configuration for mocking Telegram API.  
 \*/  
@TestConfiguration  
public class WireMockConfig {

    @Bean(initMethod \= "start", destroyMethod \= "stop")  
    public WireMockServer wireMockServer() {  
        WireMockServer server \= new WireMockServer(8089);  
          
        // Mock successful Telegram sendMessage  
        server.stubFor(post(urlPathMatching("/bot.\*/sendMessage"))  
            .willReturn(aResponse()  
                .withStatus(200)  
                .withHeader("Content-Type", "application/json")  
                .withBody("""  
                    {  
                        "ok": true,  
                        "result": {  
                            "message\_id": 12345,  
                            "chat": {"id": 123456789},  
                            "text": "Test message"  
                        }  
                    }  
                    """)));  
          
        return server;  
    }

    public static void resetMocks(WireMockServer server) {  
        server.resetAll();  
          
        // Re-configure default stub  
        server.stubFor(post(urlPathMatching("/bot.\*/sendMessage"))  
            .willReturn(aResponse()  
                .withStatus(200)  
                .withHeader("Content-Type", "application/json")  
                .withBody("{\\"ok\\":true,\\"result\\":{\\"message\_id\\":12345}}")));  
    }

    public static void simulateTelegramFailure(WireMockServer server) {  
        server.stubFor(post(urlPathMatching("/bot.\*/sendMessage"))  
            .willReturn(aResponse()  
                .withStatus(500)  
                .withBody("{\\"ok\\":false,\\"error\_code\\":500}")));  
    }  
}

**Validaciones:**

mvn test \-Dtest=BaseIntegrationTest  
\# Containers start correctly

**🔍 PUNTO DE REVISIÓN 1:**

✅ PASO 1 COMPLETADO

Configuración:  
\- TestContainers: PostgreSQL 16 \+ RabbitMQ 3.13  
\- WireMock: Telegram API mock en puerto 8089  
\- Utilidades: createTicketRequest, countTickets, waitForProcessing

Limpieza entre tests:  
\- DELETE ticket\_event, recovery\_event, outbox\_message, ticket  
\- RESET advisors a AVAILABLE

🔍 SOLICITO REVISIÓN:  
1\. ¿La configuración de containers es correcta?  
2\. ¿Puedo continuar con PASO 2?

⏸️ ESPERANDO CONFIRMACIÓN...

---

## **PASO 2: Feature \- Creación de Tickets**

**Objetivo:** Validar flujo completo de creación de tickets.

### **Escenarios Gherkin**

Feature: Creación de Tickets  
  Como usuario del sistema  
  Quiero crear un ticket de atención  
  Para ser atendido en la sucursal

  @P0 @HappyPath  
  Scenario: Crear ticket con datos válidos  
    Given el sistema está operativo  
    And hay asesores disponibles para cola "CAJA"  
    When envío POST /api/tickets con nationalId "12345678" y cola "CAJA"  
    Then recibo respuesta 201 Created  
    And el ticket tiene status "WAITING"  
    And el ticket tiene posición calculada en cola  
    And existe mensaje en Outbox con status "PENDING"

  @P0 @HappyPath  
  Scenario: Calcular posición correcta en cola con tickets existentes  
    Given existen 3 tickets en estado WAITING para cola "CAJA"  
    When creo un nuevo ticket para cola "CAJA"  
    Then el nuevo ticket tiene posición 4  
    And el tiempo estimado es 15 minutos (3 × 5 min promedio)

  @P0 @HappyPath  
  Scenario: Crear ticket sin teléfono (opcional)  
    Given el sistema está operativo  
    When envío POST /api/tickets sin campo teléfono  
    Then recibo respuesta 201 Created  
    And el ticket se crea correctamente

  @P0 @HappyPath  
  Scenario: Crear tickets para diferentes colas  
    Given hay asesores disponibles para todas las colas  
    When creo tickets para CAJA, PERSONAL, EMPRESAS y GERENCIA  
    Then cada ticket tiene su posición independiente por cola  
    And se crean 4 mensajes en Outbox con routing keys correctos

  @P1 @EdgeCase  
  Scenario: Crear ticket genera número único con prefijo de cola  
    When creo un ticket para cola "PERSONAL"  
    Then el número de ticket empieza con "P"  
    And tiene formato PXXX (3 dígitos)

  @P1 @EdgeCase  
  Scenario: Consultar ticket por código de referencia  
    Given existe un ticket creado con código de referencia conocido  
    When consulto GET /api/tickets/{codigoReferencia}  
    Then recibo los datos completos del ticket  
    And incluye posición actual y tiempo estimado

### **TicketCreationIT.java**

package com.example.ticketero.integration;

import io.restassured.response.Response;  
import org.junit.jupiter.api.DisplayName;  
import org.junit.jupiter.api.Nested;  
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;  
import static org.assertj.core.api.Assertions.assertThat;  
import static org.hamcrest.Matchers.\*;

@DisplayName("Feature: Creación de Tickets")  
class TicketCreationIT extends BaseIntegrationTest {

    @Nested  
    @DisplayName("Escenarios Happy Path (P0)")  
    class HappyPath {

        @Test  
        @DisplayName("Crear ticket con datos válidos → 201 \+ status WAITING \+ Outbox")  
        void crearTicket\_datosValidos\_debeCrearConOutbox() {  
            // Given \- Sistema operativo con asesores disponibles  
            assertThat(countAdvisorsInStatus("AVAILABLE")).isGreaterThan(0);

            // When  
            Response response \= given()  
                .contentType("application/json")  
                .body(createTicketRequest("12345678", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201)  
                .body("numero", notNullValue())  
                .body("status", equalTo("WAITING"))  
                .body("queueType", equalTo("CAJA"))  
                .body("positionInQueue", greaterThan(0))  
                .body("estimatedWaitMinutes", greaterThanOrEqualTo(0))  
                .body("codigoReferencia", notNullValue())  
                .extract().response();

            // Then \- Verificar BD  
            String numero \= response.jsonPath().getString("numero");  
            assertThat(countTicketsInStatus("WAITING")).isGreaterThanOrEqualTo(1);

            // Verificar Outbox  
            int outboxCount \= countOutboxMessages("PENDING");  
            assertThat(outboxCount).isGreaterThanOrEqualTo(1);

            // Verificar routing key  
            String routingKey \= jdbcTemplate.queryForObject(  
                "SELECT routing\_key FROM outbox\_message WHERE aggregate\_type \= 'TICKET' ORDER BY id DESC LIMIT 1",  
                String.class);  
            assertThat(routingKey).isEqualTo("caja-queue");  
        }

        @Test  
        @DisplayName("Calcular posición correcta con tickets existentes")  
        void crearTicket\_conTicketsExistentes\_debePosicionCorrecta() {  
            // Given \- Crear 3 tickets previos  
            for (int i \= 1; i \<= 3; i++) {  
                given()  
                    .contentType("application/json")  
                    .body(createTicketRequest("1000000" \+ i, "CAJA"))  
                .when()  
                    .post("/tickets")  
                .then()  
                    .statusCode(201);  
            }

            // When \- Crear ticket \#4  
            Response response \= given()  
                .contentType("application/json")  
                .body(createTicketRequest("10000004", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201)  
                .extract().response();

            // Then  
            int posicion \= response.jsonPath().getInt("positionInQueue");  
            int tiempoEstimado \= response.jsonPath().getInt("estimatedWaitMinutes");

            assertThat(posicion).isEqualTo(4);  
            // Tiempo \= (posición \- 1\) × avgTime(5) \= 3 × 5 \= 15  
            assertThat(tiempoEstimado).isEqualTo(15);  
        }

        @Test  
        @DisplayName("Crear ticket sin teléfono → debe funcionar")  
        void crearTicket\_sinTelefono\_debeCrear() {  
            // Given  
            String requestSinTelefono \= """  
                {  
                    "nationalId": "87654321",  
                    "branchOffice": "Sucursal Norte",  
                    "queueType": "PERSONAL"  
                }  
                """;

            // When \+ Then  
            given()  
                .contentType("application/json")  
                .body(requestSinTelefono)  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201)  
                .body("numero", startsWith("P"));  
        }

        @Test  
        @DisplayName("Crear tickets para diferentes colas → posiciones independientes")  
        void crearTickets\_diferentesColas\_posicionesIndependientes() {  
            // Given \+ When \- Crear un ticket por cada cola  
            String\[\] colas \= {"CAJA", "PERSONAL", "EMPRESAS", "GERENCIA"};  
            String\[\] prefijos \= {"C", "P", "E", "G"};

            for (int i \= 0; i \< colas.length; i++) {  
                Response response \= given()  
                    .contentType("application/json")  
                    .body(createTicketRequest("2000000" \+ i, colas\[i\]))  
                .when()  
                    .post("/tickets")  
                .then()  
                    .statusCode(201)  
                    .extract().response();

                // Then \- Cada cola empieza en posición 1  
                assertThat(response.jsonPath().getInt("positionInQueue")).isEqualTo(1);  
                assertThat(response.jsonPath().getString("numero")).startsWith(prefijos\[i\]);  
            }

            // Verificar 4 mensajes en Outbox  
            assertThat(countOutboxMessages("PENDING")).isGreaterThanOrEqualTo(4);  
        }  
    }

    @Nested  
    @DisplayName("Escenarios Edge Case (P1)")  
    class EdgeCases {

        @Test  
        @DisplayName("Número de ticket tiene formato correcto")  
        void crearTicket\_debeGenerarNumeroConFormato() {  
            // When  
            Response response \= given()  
                .contentType("application/json")  
                .body(createTicketRequest("11111111", "PERSONAL"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201)  
                .extract().response();

            // Then  
            String numero \= response.jsonPath().getString("numero");  
            assertThat(numero).matches("P\\\\d{3}"); // P seguido de 3 dígitos  
        }

        @Test  
        @DisplayName("Consultar ticket por código de referencia")  
        void consultarTicket\_porCodigo\_debeRetornarDatos() {  
            // Given \- Crear ticket  
            Response createResponse \= given()  
                .contentType("application/json")  
                .body(createTicketRequest("22222222", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201)  
                .extract().response();

            String codigoReferencia \= createResponse.jsonPath().getString("codigoReferencia");

            // When \+ Then  
            given()  
            .when()  
                .get("/tickets/" \+ codigoReferencia)  
            .then()  
                .statusCode(200)  
                .body("codigoReferencia", equalTo(codigoReferencia))  
                .body("status", equalTo("WAITING"))  
                .body("positionInQueue", notNullValue())  
                .body("estimatedWaitMinutes", notNullValue());  
        }  
    }  
}

**Validaciones:**

mvn test \-Dtest=TicketCreationIT  
\# Tests run: 6, Failures: 0

**🔍 PUNTO DE REVISIÓN 2:** 6 escenarios de creación implementados.

---

## **PASO 3: Feature \- Procesamiento de Tickets**

**Objetivo:** Validar flujo completo de procesamiento por workers.

### **Escenarios Gherkin**

Feature: Procesamiento de Tickets  
  Como sistema  
  Quiero procesar tickets automáticamente  
  Para asignar asesores y completar atenciones

  @P0 @HappyPath  
  Scenario: Procesar ticket completo (WAITING → COMPLETED)  
    Given existe un ticket en estado WAITING  
    And hay un asesor disponible  
    When el worker procesa el ticket  
    Then el ticket pasa por estados CALLED → IN\_PROGRESS → COMPLETED  
    And el asesor queda AVAILABLE al finalizar  
    And se incrementa el contador de tickets servidos

  @P0 @HappyPath  
  Scenario: Múltiples tickets se procesan en orden FIFO  
    Given existen 3 tickets en cola CAJA en orden de creación  
    When los workers procesan los tickets  
    Then se completan en el mismo orden de creación

  @P1 @EdgeCase  
  Scenario: Sin asesores disponibles → ticket permanece en cola  
    Given todos los asesores están BUSY  
    When se crea un nuevo ticket  
    Then el ticket permanece en WAITING  
    And el mensaje se re-encola (NACK \+ requeue)

  @P1 @EdgeCase  
  Scenario: Idempotencia \- ticket ya procesado no se reprocesa  
    Given existe un ticket en estado COMPLETED  
    When el worker intenta procesarlo nuevamente  
    Then el ticket mantiene su estado COMPLETED  
    And no se modifica el asesor

  @P1 @EdgeCase  
  Scenario: Asesor en BREAK no recibe tickets  
    Given hay 2 asesores: uno AVAILABLE y uno en BREAK  
    When se procesa un ticket  
    Then solo el asesor AVAILABLE es asignado

### **TicketProcessingIT.java**

package com.example.ticketero.integration;

import org.junit.jupiter.api.DisplayName;  
import org.junit.jupiter.api.Nested;  
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;  
import static org.assertj.core.api.Assertions.assertThat;  
import static org.awaitility.Awaitility.await;

@DisplayName("Feature: Procesamiento de Tickets")  
class TicketProcessingIT extends BaseIntegrationTest {

    @Nested  
    @DisplayName("Escenarios Happy Path (P0)")  
    class HappyPath {

        @Test  
        @DisplayName("Procesar ticket completo → WAITING → COMPLETED")  
        void procesarTicket\_debeCompletarFlujo() {  
            // Given \- Asesores disponibles  
            int asesoresDisponibles \= countAdvisorsInStatus("AVAILABLE");  
            assertThat(asesoresDisponibles).isGreaterThan(0);

            // When \- Crear ticket (worker lo procesará automáticamente)  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("33333333", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201);

            // Then \- Esperar procesamiento completo  
            await()  
                .atMost(30, TimeUnit.SECONDS)  
                .pollInterval(1, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 1);

            // Verificar asesor liberado  
            assertThat(countAdvisorsInStatus("AVAILABLE")).isEqualTo(asesoresDisponibles);

            // Verificar contador incrementado  
            Integer totalServed \= jdbcTemplate.queryForObject(  
                "SELECT SUM(total\_tickets\_served) FROM advisor",  
                Integer.class);  
            assertThat(totalServed).isGreaterThan(0);  
        }

        @Test  
        @DisplayName("Múltiples tickets se procesan en orden FIFO")  
        void procesarTickets\_debenSerFIFO() {  
            // Given \- Crear 3 tickets en orden  
            String\[\] nationalIds \= {"44444441", "44444442", "44444443"};  
              
            for (String id : nationalIds) {  
                given()  
                    .contentType("application/json")  
                    .body(createTicketRequest(id, "CAJA"))  
                .when()  
                    .post("/tickets")  
                .then()  
                    .statusCode(201);  
                  
                // Pequeña pausa para garantizar orden  
                try { Thread.sleep(100); } catch (InterruptedException e) {}  
            }

            // When \- Esperar que todos se completen  
            await()  
                .atMost(60, TimeUnit.SECONDS)  
                .pollInterval(2, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 3);

            // Then \- Verificar orden por completed\_at  
            var completedOrder \= jdbcTemplate.queryForList(  
                "SELECT national\_id FROM ticket WHERE status \= 'COMPLETED' ORDER BY completed\_at ASC",  
                String.class);

            // El primero en crearse debería ser el primero en completarse  
            assertThat(completedOrder.get(0)).isEqualTo("44444441");  
        }  
    }

    @Nested  
    @DisplayName("Escenarios Edge Case (P1)")  
    class EdgeCases {

        @Test  
        @DisplayName("Sin asesores disponibles → ticket permanece WAITING")  
        void sinAsesores\_ticketPermanece() {  
            // Given \- Poner todos los asesores en BUSY  
            jdbcTemplate.execute("UPDATE advisor SET status \= 'BUSY'");  
            assertThat(countAdvisorsInStatus("AVAILABLE")).isZero();

            // When \- Crear ticket  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("55555555", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201);

            // Then \- Esperar un poco y verificar que sigue WAITING  
            try { Thread.sleep(5000); } catch (InterruptedException e) {}  
              
            assertThat(countTicketsInStatus("WAITING")).isGreaterThanOrEqualTo(1);  
            assertThat(countTicketsInStatus("COMPLETED")).isZero();

            // Cleanup \- Restaurar asesores  
            jdbcTemplate.execute("UPDATE advisor SET status \= 'AVAILABLE'");  
        }

        @Test  
        @DisplayName("Idempotencia \- ticket COMPLETED no se reprocesa")  
        void ticketCompletado\_noSeReprocesa() {  
            // Given \- Crear y esperar que se complete  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("66666666", "CAJA"))  
            .when()  
                .post("/tickets");

            await()  
                .atMost(30, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 1);

            // Guardar estado actual  
            int totalServedBefore \= jdbcTemplate.queryForObject(  
                "SELECT SUM(total\_tickets\_served) FROM advisor",  
                Integer.class);

            // When \- Esperar más tiempo (si se reprocesara, cambiaría)  
            try { Thread.sleep(5000); } catch (InterruptedException e) {}

            // Then \- Nada debe haber cambiado  
            int totalServedAfter \= jdbcTemplate.queryForObject(  
                "SELECT SUM(total\_tickets\_served) FROM advisor",  
                Integer.class);  
              
            assertThat(totalServedAfter).isEqualTo(totalServedBefore);  
        }

        @Test  
        @DisplayName("Asesor en BREAK no recibe tickets")  
        void asesorEnBreak\_noRecibeTickets() {  
            // Given \- Poner un asesor en BREAK  
            jdbcTemplate.execute("UPDATE advisor SET status \= 'BREAK' WHERE id \= 1");  
            int availableBefore \= countAdvisorsInStatus("AVAILABLE");

            // When \- Crear ticket  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("77777777", "CAJA"))  
            .when()  
                .post("/tickets");

            // Esperar procesamiento  
            await()  
                .atMost(30, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 1);

            // Then \- El asesor en BREAK no debe haber sido asignado  
            String breakAdvisorStatus \= jdbcTemplate.queryForObject(  
                "SELECT status FROM advisor WHERE id \= 1",  
                String.class);  
            assertThat(breakAdvisorStatus).isEqualTo("BREAK");

            // Cleanup  
            jdbcTemplate.execute("UPDATE advisor SET status \= 'AVAILABLE' WHERE id \= 1");  
        }  
    }  
}

**🔍 PUNTO DE REVISIÓN 3:** 5 escenarios de procesamiento implementados.

---

## **PASO 4: Feature \- Notificaciones Telegram**

**Objetivo:** Validar envío de las 3 notificaciones automáticas.

### **Escenarios Gherkin**

Feature: Notificaciones Telegram  
  Como usuario  
  Quiero recibir notificaciones de mi turno  
  Para saber cuándo ser atendido

  @P0 @HappyPath  
  Scenario: Notificación \#1 \- Confirmación al crear ticket  
    Given creo un ticket con teléfono válido  
    Then se envía notificación de confirmación via Telegram  
    And el mensaje incluye número de ticket y posición

  @P0 @HappyPath  
  Scenario: Notificación \#2 \- Próximo turno (posición ≤ 3\)  
    Given mi ticket está en posición 3  
    When el ticket anterior se completa  
    And mi posición pasa a 2  
    Then recibo notificación "Tu turno está próximo"

  @P0 @HappyPath  
  Scenario: Notificación \#3 \- Es tu turno  
    Given mi ticket está siendo procesado  
    When el asesor me llama  
    Then recibo notificación con asesor y módulo asignado

  @P1 @EdgeCase  
  Scenario: Telegram API caída → mensaje queda en Outbox FAILED  
    Given Telegram API está caído  
    When se intenta enviar notificación  
    Then después de reintentos, mensaje queda FAILED  
    And el ticket sigue su flujo normal

### **NotificationIT.java**

package com.example.ticketero.integration;

import com.example.ticketero.config.WireMockConfig;  
import com.github.tomakehurst.wiremock.WireMockServer;  
import com.github.tomakehurst.wiremock.client.WireMock;  
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.DisplayName;  
import org.junit.jupiter.api.Nested;  
import org.junit.jupiter.api.Test;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.\*;  
import static io.restassured.RestAssured.given;  
import static org.assertj.core.api.Assertions.assertThat;  
import static org.awaitility.Awaitility.await;

@DisplayName("Feature: Notificaciones Telegram")  
@Import(WireMockConfig.class)  
class NotificationIT extends BaseIntegrationTest {

    @Autowired  
    private WireMockServer wireMockServer;

    @BeforeEach  
    void resetWireMock() {  
        WireMockConfig.resetMocks(wireMockServer);  
    }

    @Nested  
    @DisplayName("Escenarios Happy Path (P0)")  
    class HappyPath {

        @Test  
        @DisplayName("Notificación \#1 \- Confirmación al crear ticket")  
        void crearTicket\_debeEnviarNotificacion() {  
            // Given  
            wireMockServer.resetRequests();

            // When  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("88888888", "+56912345678", "Sucursal Centro", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201);

            // Then \- Esperar y verificar llamada a Telegram  
            await()  
                .atMost(5, TimeUnit.SECONDS)  
                .untilAsserted(() \-\> {  
                    wireMockServer.verify(  
                        postRequestedFor(urlPathMatching("/bot.\*/sendMessage"))  
                            .withRequestBody(containing("Ticket Creado"))  
                    );  
                });  
        }

        @Test  
        @DisplayName("Notificación \#3 \- Es tu turno (incluye asesor y módulo)")  
        void procesarTicket\_debeNotificarTurnoActivo() {  
            // Given  
            wireMockServer.resetRequests();

            // When \- Crear ticket y esperar procesamiento  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("99999999", "+56987654321", "Sucursal Norte", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201);

            // Then \- Esperar notificación de turno activo  
            await()  
                .atMost(30, TimeUnit.SECONDS)  
                .untilAsserted(() \-\> {  
                    wireMockServer.verify(  
                        postRequestedFor(urlPathMatching("/bot.\*/sendMessage"))  
                            .withRequestBody(containing("ES TU TURNO"))  
                    );  
                });  
        }

        @Test  
        @DisplayName("Notificación \#2 \- Próximo turno cuando posición ≤ 3")  
        void posicionProxima\_debeNotificarProximoTurno() {  
            // Given \- Crear 4 tickets (el 4to tendrá posición 4\)  
            for (int i \= 1; i \<= 4; i++) {  
                given()  
                    .contentType("application/json")  
                    .body(createTicketRequest("1111111" \+ i, "+5691234567" \+ i, "Centro", "CAJA"))  
                .when()  
                    .post("/tickets");  
            }

            wireMockServer.resetRequests();

            // When \- Esperar que se procesen algunos tickets  
            await()  
                .atMost(60, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 1);

            // Then \- Debería haberse enviado notificación de próximo turno  
            // cuando el ticket 4 pasó a posición ≤ 3  
            await()  
                .atMost(10, TimeUnit.SECONDS)  
                .untilAsserted(() \-\> {  
                    wireMockServer.verify(  
                        atLeast(1),  
                        postRequestedFor(urlPathMatching("/bot.\*/sendMessage"))  
                            .withRequestBody(containing("próximo"))  
                    );  
                });  
        }  
    }

    @Nested  
    @DisplayName("Escenarios Edge Case (P1)")  
    class EdgeCases {

        @Test  
        @DisplayName("Telegram caído → ticket sigue su flujo, notificación falla silenciosamente")  
        void telegramCaido\_ticketContinua() {  
            // Given \- Simular fallo de Telegram  
            WireMockConfig.simulateTelegramFailure(wireMockServer);

            // When \- Crear ticket  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("10101010", "+56911111111", "Centro", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(201);

            // Then \- El ticket debe seguir procesándose normalmente  
            await()  
                .atMost(30, TimeUnit.SECONDS)  
                .until(() \-\> countTicketsInStatus("COMPLETED") \>= 1);

            // Verificar que el ticket se completó a pesar del fallo de Telegram  
            int completed \= countTicketsInStatus("COMPLETED");  
            assertThat(completed).isGreaterThanOrEqualTo(1);  
        }  
    }  
}

**🔍 PUNTO DE REVISIÓN 4:** 4 escenarios de notificaciones implementados.

---

## **PASO 5: Feature \- Validaciones de Input**

**Objetivo:** Validar reglas de negocio y restricciones de entrada.

### **Escenarios Gherkin**

Feature: Validaciones de Input  
  Como sistema  
  Quiero validar los datos de entrada  
  Para mantener integridad de datos

  @P1 @Validation  
  Scenario Outline: nationalId debe tener 8-12 dígitos  
    When envío POST con nationalId "\<valor\>"  
    Then recibo respuesta \<codigo\>  
      
    Examples:  
      | valor          | codigo |  
      | 1234567        | 400    | \# 7 dígitos \- muy corto  
      | 12345678       | 201    | \# 8 dígitos \- válido  
      | 123456789012   | 201    | \# 12 dígitos \- válido  
      | 1234567890123  | 400    | \# 13 dígitos \- muy largo  
      | 12345ABC       | 400    | \# contiene letras

  @P1 @Validation  
  Scenario: Campo queueType inválido  
    When envío POST con queueType "INVALIDO"  
    Then recibo respuesta 400  
    And el mensaje indica error de validación

  @P1 @Validation  
  Scenario: Campo branchOffice vacío  
    When envío POST sin branchOffice  
    Then recibo respuesta 400

  @P1 @Validation  
  Scenario: Ticket no encontrado  
    When consulto GET /api/tickets/{uuid-inexistente}  
    Then recibo respuesta 404

### **ValidationIT.java**

package com.example.ticketero.integration;

import org.junit.jupiter.api.DisplayName;  
import org.junit.jupiter.api.Nested;  
import org.junit.jupiter.api.Test;  
import org.junit.jupiter.params.ParameterizedTest;  
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static io.restassured.RestAssured.given;  
import static org.hamcrest.Matchers.\*;

@DisplayName("Feature: Validaciones de Input")  
class ValidationIT extends BaseIntegrationTest {

    @Nested  
    @DisplayName("Validación de nationalId")  
    class NationalIdValidation {

        @ParameterizedTest(name \= "nationalId={0} → HTTP {1}")  
        @CsvSource({  
            "1234567, 400",      // 7 dígitos \- muy corto  
            "12345678, 201",     // 8 dígitos \- válido (límite inferior)  
            "123456789, 201",    // 9 dígitos \- válido  
            "123456789012, 201", // 12 dígitos \- válido (límite superior)  
            "1234567890123, 400" // 13 dígitos \- muy largo  
        })  
        @DisplayName("Validar longitud de nationalId")  
        void validarLongitud\_nationalId(String nationalId, int expectedStatus) {  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest(nationalId, "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(expectedStatus);  
        }

        @Test  
        @DisplayName("nationalId con letras → 400")  
        void nationalId\_conLetras\_debeRechazar() {  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("12345ABC", "CAJA"))  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(400)  
                .body("message", containsString("nationalId"));  
        }

        @Test  
        @DisplayName("nationalId vacío → 400")  
        void nationalId\_vacio\_debeRechazar() {  
            String request \= """  
                {  
                    "nationalId": "",  
                    "branchOffice": "Centro",  
                    "queueType": "CAJA"  
                }  
                """;

            given()  
                .contentType("application/json")  
                .body(request)  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(400);  
        }  
    }

    @Nested  
    @DisplayName("Validación de queueType")  
    class QueueTypeValidation {

        @Test  
        @DisplayName("queueType inválido → 400")  
        void queueType\_invalido\_debeRechazar() {  
            String request \= """  
                {  
                    "nationalId": "12345678",  
                    "branchOffice": "Centro",  
                    "queueType": "INVALIDO"  
                }  
                """;

            given()  
                .contentType("application/json")  
                .body(request)  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(400);  
        }

        @Test  
        @DisplayName("queueType null → 400")  
        void queueType\_null\_debeRechazar() {  
            String request \= """  
                {  
                    "nationalId": "12345678",  
                    "branchOffice": "Centro"  
                }  
                """;

            given()  
                .contentType("application/json")  
                .body(request)  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(400);  
        }  
    }

    @Nested  
    @DisplayName("Validación de campos requeridos")  
    class RequiredFieldsValidation {

        @Test  
        @DisplayName("branchOffice vacío → 400")  
        void branchOffice\_vacio\_debeRechazar() {  
            String request \= """  
                {  
                    "nationalId": "12345678",  
                    "branchOffice": "",  
                    "queueType": "CAJA"  
                }  
                """;

            given()  
                .contentType("application/json")  
                .body(request)  
            .when()  
                .post("/tickets")  
            .then()  
                .statusCode(400);  
        }  
    }

    @Nested  
    @DisplayName("Recursos no encontrados")  
    class NotFoundValidation {

        @Test  
        @DisplayName("Ticket inexistente → 404")  
        void ticket\_inexistente\_debe404() {  
            UUID uuidInexistente \= UUID.randomUUID();

            given()  
            .when()  
                .get("/tickets/" \+ uuidInexistente)  
            .then()  
                .statusCode(404)  
                .body("message", containsString(uuidInexistente.toString()));  
        }  
    }  
}

**🔍 PUNTO DE REVISIÓN 5:** 5 escenarios de validación implementados.

---

## **PASO 6: Feature \- Dashboard Admin**

**Objetivo:** Validar endpoints administrativos.

### **Escenarios Gherkin**

Feature: Dashboard Administrativo  
  Como administrador  
  Quiero ver el estado del sistema  
  Para monitorear las operaciones

  @P2 @Admin  
  Scenario: Ver dashboard general  
    Given existen tickets en diferentes estados  
    When consulto GET /api/admin/dashboard  
    Then recibo resumen por cola y estadísticas de asesores

  @P2 @Admin  
  Scenario: Ver estado de cola específica  
    Given existen 5 tickets en cola CAJA  
    When consulto GET /api/admin/queues/CAJA  
    Then recibo lista de tickets activos en esa cola

  @P2 @Admin  
  Scenario: Cambiar estado de asesor  
    Given existe un asesor en estado AVAILABLE  
    When envío PUT /api/admin/advisors/{id}/status?status=BREAK  
    Then el asesor cambia a estado BREAK

  @P2 @Admin  
  Scenario: Ver estadísticas de asesores  
    Given hay asesores con tickets atendidos  
    When consulto GET /api/admin/advisors/stats  
    Then recibo total, disponibles, ocupados y tickets atendidos

### **AdminDashboardIT.java**

package com.example.ticketero.integration;

import org.junit.jupiter.api.DisplayName;  
import org.junit.jupiter.api.Nested;  
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;  
import static org.hamcrest.Matchers.\*;

@DisplayName("Feature: Dashboard Administrativo")  
class AdminDashboardIT extends BaseIntegrationTest {

    @Nested  
    @DisplayName("Dashboard General")  
    class DashboardGeneral {

        @Test  
        @DisplayName("GET /api/admin/dashboard → estado del sistema")  
        void dashboard\_debeRetornarEstado() {  
            // Given \- Crear algunos tickets  
            given()  
                .contentType("application/json")  
                .body(createTicketRequest("20000001", "CAJA"))  
            .when()  
                .post("/tickets");

            given()  
                .contentType("application/json")  
                .body(createTicketRequest("20000002", "PERSONAL"))  
            .when()  
                .post("/tickets");

            // When \+ Then  
            given()  
            .when()  
                .get("/admin/dashboard")  
            .then()  
                .statusCode(200)  
                .body("ticketsPorCola", notNullValue())  
                .body("estadisticasAsesores", notNullValue())  
                .body("timestamp", notNullValue());  
        }  
    }

    @Nested  
    @DisplayName("Estado de Colas")  
    class EstadoColas {

        @Test  
        @DisplayName("GET /api/admin/queues/CAJA → tickets de la cola")  
        void colaEspecifica\_debeRetornarTickets() {  
            // Given \- Crear tickets en cola CAJA  
            for (int i \= 1; i \<= 3; i++) {  
                given()  
                    .contentType("application/json")  
                    .body(createTicketRequest("3000000" \+ i, "CAJA"))  
                .when()  
                    .post("/tickets");  
            }

            // When \+ Then  
            given()  
            .when()  
                .get("/admin/queues/CAJA")  
            .then()  
                .statusCode(200)  
                .body("queueType", equalTo("CAJA"))  
                .body("totalActivos", greaterThanOrEqualTo(0));  
        }

        @Test  
        @DisplayName("GET /api/admin/queues/CAJA/stats → estadísticas")  
        void estadisticasCola\_debeRetornarMetricas() {  
            given()  
            .when()  
                .get("/admin/queues/CAJA/stats")  
            .then()  
                .statusCode(200)  
                .body("queueType", equalTo("CAJA"))  
                .body("waiting", greaterThanOrEqualTo(0))  
                .body("completed", greaterThanOrEqualTo(0))  
                .body("avgServiceTimeMinutes", greaterThan(0));  
        }  
    }

    @Nested  
    @DisplayName("Gestión de Asesores")  
    class GestionAsesores {

        @Test  
        @DisplayName("PUT /api/admin/advisors/{id}/status → cambiar estado")  
        void cambiarEstado\_debeActualizar() {  
            // Given \- Verificar que existe asesor 1  
            Long advisorId \= jdbcTemplate.queryForObject(  
                "SELECT id FROM advisor LIMIT 1", Long.class);

            // When  
            given()  
                .queryParam("status", "BREAK")  
            .when()  
                .put("/admin/advisors/" \+ advisorId \+ "/status")  
            .then()  
                .statusCode(200)  
                .body(containsString("BREAK"));

            // Then \- Verificar en BD  
            String status \= jdbcTemplate.queryForObject(  
                "SELECT status FROM advisor WHERE id \= ?",  
                String.class, advisorId);  
            org.assertj.core.api.Assertions.assertThat(status).isEqualTo("BREAK");

            // Cleanup  
            jdbcTemplate.update(  
                "UPDATE advisor SET status \= 'AVAILABLE' WHERE id \= ?", advisorId);  
        }

        @Test  
        @DisplayName("GET /api/admin/advisors/stats → estadísticas")  
        void estadisticasAsesores\_debeRetornarMetricas() {  
            given()  
            .when()  
                .get("/admin/advisors/stats")  
            .then()  
                .statusCode(200)  
                .body("total", greaterThan(0))  
                .body("disponibles", greaterThanOrEqualTo(0))  
                .body("ocupados", greaterThanOrEqualTo(0))  
                .body("totalTicketsAtendidos", greaterThanOrEqualTo(0));  
        }  
    }  
}

**🔍 PUNTO DE REVISIÓN 6:** 4 escenarios de admin implementados.

---

## **PASO 7: Ejecución Final y Reporte**

**Objetivo:** Ejecutar todos los tests E2E y generar reporte.

**Comandos:**

\# 1\. Ejecutar todos los tests de integración  
mvn test \-Dtest="\*IT" \-Dtest.groups=integration

\# 2\. Generar reporte HTML  
mvn surefire-report:report

\# 3\. Ver reporte  
open target/site/surefire-report.html

**Resultados Esperados:**

\[INFO\] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0

Tests por feature:  
\- TicketCreationIT: 6 tests  
\- TicketProcessingIT: 5 tests  
\- NotificationIT: 4 tests  
\- ValidationIT: 5 tests  
\- AdminDashboardIT: 4 tests

**Matriz de Cobertura de Escenarios:**

| Feature | Happy Path | Edge Cases | Errors | Total |
| ----- | ----- | ----- | ----- | ----- |
| Creación Tickets | 4 | 2 | 0 | 6 |
| Procesamiento | 2 | 3 | 0 | 5 |
| Notificaciones | 3 | 1 | 0 | 4 |
| Validaciones | 0 | 0 | 5 | 5 |
| Admin Dashboard | 4 | 0 | 0 | 4 |
| **Total** | **13 (54%)** | **6 (25%)** | **5 (21%)** | **24** |

**🔍 PUNTO DE REVISIÓN FINAL 7:**

✅ PASO 7 COMPLETADO \- TESTS E2E COMPLETOS

Tests totales: 24 escenarios  
Features cubiertos: 5/5

Distribución:  
\- Happy Path: 54%  
\- Edge Cases: 25%  
\- Error Handling: 21%

Infraestructura validada:  
\- ✅ PostgreSQL 16 (TestContainers)  
\- ✅ RabbitMQ 3.13 (TestContainers)  
\- ✅ Telegram API (WireMock)  
\- ✅ API REST (RestAssured)

Flujos E2E validados:  
\- ✅ Crear ticket → Outbox → RabbitMQ → Worker → Completar  
\- ✅ Notificaciones en cada etapa  
\- ✅ Validaciones de input  
\- ✅ Dashboard administrativo

⏸️ TESTS E2E COMPLETADOS\!

---

## **Resumen de Escenarios**

| Paso | Feature | Escenarios | Prioridad |
| ----- | ----- | ----- | ----- |
| 1 | Setup TestContainers | \- | \- |
| 2 | Creación de Tickets | 6 | P0-P1 |
| 3 | Procesamiento | 5 | P0-P1 |
| 4 | Notificaciones | 4 | P0-P1 |
| 5 | Validaciones | 5 | P1 |
| 6 | Admin Dashboard | 4 | P2 |
| **Total** | **5 features** | **24** | \- |

---

## **Criterios de Aceptación por Test**

Cada test valida:

| Criterio | Descripción |
| ----- | ----- |
| ✅ HTTP Status | 200, 201, 400, 404 según escenario |
| ✅ JSON Response | Estructura y campos esperados |
| ✅ Estado BD | Ticket, Advisor, OutboxMessage |
| ✅ RabbitMQ | Mensaje encolado (implícito vía procesamiento) |
| ✅ Telegram | Llamadas verificadas con WireMock |

---

## **Datos de Prueba**

**Formato chileno:**

* nationalId: 8-12 dígitos (ej: "12345678")  
* telefono: "+569XXXXXXXX" (ej: "+56912345678")  
* Sucursales: "Sucursal Centro", "Sucursal Norte"

**Colas disponibles:**

* CAJA (prefijo C)  
* PERSONAL (prefijo P)  
* EMPRESAS (prefijo E)  
* GERENCIA (prefijo G)

---

## **Comandos Útiles**

\# Ejecutar un test específico  
mvn test \-Dtest=TicketCreationIT

\# Ejecutar con logs detallados  
mvn test \-Dtest=TicketProcessingIT \-X

\# Ver logs de containers  
docker logs $(docker ps \-q \--filter ancestor=postgres:16-alpine)

\# Ejecutar solo tests P0  
mvn test \-Dgroups=P0

---

**Tiempo estimado:** 5-6 horas
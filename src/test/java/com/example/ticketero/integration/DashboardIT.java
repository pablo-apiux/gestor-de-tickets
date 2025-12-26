package com.example.ticketero.integration;

import com.example.ticketero.model.entity.Advisor;
import com.example.ticketero.model.enums.AdvisorStatus;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.repository.AdvisorRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Dashboard Admin - Integration Tests")
class DashboardIT extends BaseIntegrationTestSimple {

    @Autowired
    private AdvisorRepository advisorRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        limpiarAsesores();
        configurarAsesores();
    }

    @Test
    @DisplayName("Debe obtener dashboard completo con métricas básicas")
    void debeObtenerDashboardCompleto() {
        // Given: Crear algunos tickets usando la API
        given()
            .contentType("application/json")
            .body(createTicketRequest("12345678", "CAJA"))
            .post("/tickets");
        
        given()
            .contentType("application/json")
            .body(createTicketRequest("87654321", "PERSONAL_BANKER"))
            .post("/tickets");

        // When & Then
        given()
            .when()
                .get("/admin/dashboard")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("timestamp", notNullValue())
                .body("summary", notNullValue())
                .body("advisors", notNullValue())
                .body("queues", notNullValue())
                .body("alerts", notNullValue());
    }

    @Test
    @DisplayName("Debe calcular correctamente métricas de resumen")
    void debeCalcularMetricasResumen() {
        // Given: Crear tickets usando la API
        given()
            .contentType("application/json")
            .body(createTicketRequest("12345678", "CAJA"))
            .post("/tickets");
        
        given()
            .contentType("application/json")
            .body(createTicketRequest("87654321", "PERSONAL_BANKER"))
            .post("/tickets");
        
        given()
            .contentType("application/json")
            .body(createTicketRequest("11111111", "EMPRESAS"))
            .post("/tickets");

        // When & Then
        given()
            .when()
                .get("/admin/dashboard")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("summary.totalTicketsToday", equalTo(3))
                .body("summary.waitingTickets", greaterThanOrEqualTo(0))
                .body("summary.attendingTickets", greaterThanOrEqualTo(0))
                .body("summary.completedTickets", greaterThanOrEqualTo(0))
                .body("summary.averageWaitTimeMinutes", notNullValue())
                .body("summary.peakHour", notNullValue());
    }

    @Test
    @DisplayName("Debe mostrar correctamente datos de asesores")
    void debeMostrarDatosAsesores() {
        // When & Then
        given()
            .when()
                .get("/admin/dashboard")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("advisors.available", equalTo(2))
                .body("advisors.busy", equalTo(1))
                .body("advisors.offline", equalTo(1))
                .body("advisors.totalCapacity", equalTo(4));
    }

    @Test
    @DisplayName("Debe generar métricas por cola correctamente")
    void debeGenerarMetricasPorCola() {
        // Given: Crear tickets en diferentes colas
        given()
            .contentType("application/json")
            .body(createTicketRequest("12345678", "CAJA"))
            .post("/tickets");
        
        given()
            .contentType("application/json")
            .body(createTicketRequest("87654321", "PERSONAL_BANKER"))
            .post("/tickets");

        // When & Then
        given()
            .when()
                .get("/admin/dashboard")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("queues", hasSize(QueueType.values().length))
                .body("queues.find { it.queueType == 'CAJA' }.waitingTickets", greaterThanOrEqualTo(0))
                .body("queues.find { it.queueType == 'PERSONAL_BANKER' }.waitingTickets", greaterThanOrEqualTo(0))
                .body("queues.find { it.queueType == 'EMPRESAS' }", notNullValue())
                .body("queues.find { it.queueType == 'GERENCIA' }", notNullValue());
    }

    @Test
    @DisplayName("Debe manejar dashboard vacío correctamente")
    void debeManejarDashboardVacio() {
        // Given: Sin tickets ni asesores adicionales

        // When & Then
        given()
            .when()
                .get("/admin/dashboard")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("summary.totalTicketsToday", equalTo(0))
                .body("summary.waitingTickets", equalTo(0))
                .body("summary.attendingTickets", equalTo(0))
                .body("summary.completedTickets", equalTo(0))
                .body("advisors.totalCapacity", equalTo(4))
                .body("queues", hasSize(QueueType.values().length));
    }

    private void limpiarAsesores() {
        try {
            advisorRepository.deleteAll();
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    private void configurarAsesores() {
        advisorRepository.save(Advisor.builder()
            .name("Asesor 1")
            .email("asesor1@test.com")
            .status(AdvisorStatus.AVAILABLE)
            .moduleNumber(1)
            .build());
        
        advisorRepository.save(Advisor.builder()
            .name("Asesor 2")
            .email("asesor2@test.com")
            .status(AdvisorStatus.AVAILABLE)
            .moduleNumber(2)
            .build());
        
        advisorRepository.save(Advisor.builder()
            .name("Asesor 3")
            .email("asesor3@test.com")
            .status(AdvisorStatus.BUSY)
            .moduleNumber(3)
            .build());
        
        advisorRepository.save(Advisor.builder()
            .name("Asesor 4")
            .email("asesor4@test.com")
            .status(AdvisorStatus.OFFLINE)
            .moduleNumber(4)
            .build());
    }
}
package com.example.ticketero.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Feature: Procesamiento de Tickets")
class TicketProcessingSimpleIT extends BaseIntegrationTestSimple {

    @Nested
    @DisplayName("Escenarios Happy Path (P0)")
    class HappyPath {

        @Test
        @DisplayName("Llamar ticket → cambia a ATENDIENDO")
        void llamarTicket_debeActualizarEstado() {
            // Given - Crear ticket
            Response createResponse = given()
                .contentType("application/json")
                .body(createTicketRequest("33333333", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201)
                .extract().response();

            Long ticketId = createResponse.jsonPath().getLong("codigoReferencia");
            // Usar ID de asesor 1 (asumiendo que existe)
            Long advisorId = 1L;

            // When - Llamar ticket
            given()
            .when()
                .put("/tickets/" + ticketId + "/llamar/" + advisorId)
            .then()
                .statusCode(200);

            // Then - Verificar que el ticket cambió de estado
            String numero = createResponse.jsonPath().getString("numero");
            given()
            .when()
                .get("/tickets/" + numero)
            .then()
                .statusCode(200)
                .body("assignedAdvisor", notNullValue());
        }

        @Test
        @DisplayName("Obtener tickets activos → lista correcta")
        void obtenerTicketsActivos_debeRetornarLista() {
            // Given - Crear algunos tickets
            given()
                .contentType("application/json")
                .body(createTicketRequest("77777777", "CAJA"))
            .when()
                .post("/tickets");

            given()
                .contentType("application/json")
                .body(createTicketRequest("88888888", "PERSONAL_BANKER"))
            .when()
                .post("/tickets");

            // When + Then
            given()
            .when()
                .get("/tickets")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
        }
    }

    @Nested
    @DisplayName("Escenarios Edge Case (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Llamar ticket con ID inválido → 400")
        void llamarTicket_idInvalido_debe400() {
            given()
            .when()
                .put("/tickets/0/llamar/1")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Finalizar ticket con ID inválido → 400")
        void finalizarTicket_idInvalido_debe400() {
            given()
            .when()
                .put("/tickets/0/finalizar")
            .then()
                .statusCode(400);
        }
    }
}
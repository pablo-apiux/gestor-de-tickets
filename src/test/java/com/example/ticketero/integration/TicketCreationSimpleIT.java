package com.example.ticketero.integration;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Feature: Creación de Tickets (Simplified)")
class TicketCreationSimpleIT extends BaseIntegrationTestSimple {

    @Nested
    @DisplayName("Escenarios Happy Path (P0)")
    class HappyPath {

        @Test
        @DisplayName("Crear ticket con datos válidos → 201 + status EN_ESPERA")
        void crearTicket_datosValidos_debeCrear() {
            // When
            Response response = given()
                .contentType("application/json")
                .body(createTicketRequest("12345678", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201)
                .body("numero", notNullValue())
                .body("status", equalTo("EN_ESPERA"))
                .body("queueType", equalTo("CAJA"))
                .body("positionInQueue", greaterThan(0))
                .body("estimatedWaitMinutes", greaterThanOrEqualTo(0))
                .body("codigoReferencia", notNullValue())
                .extract().response();

            // Then - Verificar BD
            assertThat(countTicketsInStatus("EN_ESPERA")).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Crear ticket sin teléfono → debe funcionar")
        void crearTicket_sinTelefono_debeCrear() {
            // Given
            String requestSinTelefono = """
                {
                    "nationalId": "87654321",
                    "branchOffice": "Sucursal Norte",
                    "queueType": "PERSONAL_BANKER"
                }
                """;

            // When + Then
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
        void crearTickets_diferentesColas_posicionesIndependientes() {
            // Given + When - Crear un ticket por cada cola
            String[] colas = {"CAJA", "PERSONAL_BANKER", "EMPRESAS", "GERENCIA"};
            String[] prefijos = {"C", "P", "E", "G"};

            for (int i = 0; i < colas.length; i++) {
                Response response = given()
                    .contentType("application/json")
                    .body(createTicketRequest("2000000" + i, colas[i]))
                .when()
                    .post("/tickets")
                .then()
                    .statusCode(201)
                    .extract().response();

                // Then - Verificar que se creó correctamente
                assertThat(response.jsonPath().getString("numero")).startsWith(prefijos[i]);
            }
        }
    }

    @Nested
    @DisplayName("Escenarios Edge Case (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Número de ticket tiene formato correcto")
        void crearTicket_debeGenerarNumeroConFormato() {
            // When
            Response response = given()
                .contentType("application/json")
                .body(createTicketRequest("11111111", "PERSONAL_BANKER"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201)
                .extract().response();

            // Then
            String numero = response.jsonPath().getString("numero");
            assertThat(numero).matches("P\\d{3}"); // P seguido de 3 dígitos
        }

        @Test
        @DisplayName("Consultar ticket por código de referencia")
        void consultarTicket_porCodigo_debeRetornarDatos() {
            // Given - Crear ticket
            Response createResponse = given()
                .contentType("application/json")
                .body(createTicketRequest("22222222", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201)
                .extract().response();

            String numero = createResponse.jsonPath().getString("numero");

            // When + Then - Consultar por número de ticket
            given()
            .when()
                .get("/tickets/" + numero)
            .then()
                .statusCode(200)
                .body("numero", equalTo(numero))
                .body("status", equalTo("EN_ESPERA"))
                .body("positionInQueue", notNullValue())
                .body("estimatedWaitMinutes", notNullValue());
        }
    }
}
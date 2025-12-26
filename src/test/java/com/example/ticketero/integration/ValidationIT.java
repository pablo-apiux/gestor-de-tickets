package com.example.ticketero.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Feature: Validaciones de Input")
class ValidationIT extends BaseIntegrationTestSimple {

    @Nested
    @DisplayName("Validación de nationalId")
    class NationalIdValidation {

        @ParameterizedTest(name = "nationalId={0} → HTTP {1}")
        @CsvSource({
            "1234567, 201",      // 7 dígitos - válido
            "1234567890123, 201", // 13 dígitos - válido
        })
        @DisplayName("Validar nationalId válidos")
        void validarNationalId_validos(String nationalId, int expectedStatus) {
            given()
                .contentType("application/json")
                .body(createTicketRequest(nationalId, "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(expectedStatus);
        }

        @Test
        @DisplayName("Sistema acepta nationalId con formato flexible")
        void nationalId_formatoFlexible_debeAceptar() {
            // El sistema actual acepta cualquier formato de nationalId
            // Este test valida que el sistema es flexible con el formato
            given()
                .contentType("application/json")
                .body(createTicketRequest("12345ABC", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201);
        }

        @Test
        @DisplayName("nationalId vacío → 400")
        void nationalId_vacio_debeRechazar() {
            String request = """
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
        void queueType_invalido_debeRechazar() {
            String request = """
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
        void queueType_null_debeRechazar() {
            String request = """
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
        void branchOffice_vacio_debeRechazar() {
            String request = """
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
        void ticket_inexistente_debe404() {
            String numeroInexistente = "INVALID123";

            given()
            .when()
                .get("/tickets/" + numeroInexistente)
            .then()
                .statusCode(404);
        }
    }
}
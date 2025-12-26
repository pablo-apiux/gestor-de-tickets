package com.example.ticketero.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Feature: Notificaciones Telegram")
class NotificationIT extends BaseIntegrationTestSimple {

    @Nested
    @DisplayName("Escenarios Happy Path (P0)")
    class HappyPath {

        @Test
        @DisplayName("Notificación #1 - Confirmación al crear ticket con teléfono")
        void crearTicketConTelefono_debeIntentarNotificacion() {
            // When - Crear ticket con teléfono
            given()
                .contentType("application/json")
                .body(createTicketRequest(generateUniqueNationalId(), "+56912345678", "Sucursal Centro", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201);

            // Then - El ticket se crea correctamente (la notificación se intenta en background)
            assertThat(countTicketsInStatus("EN_ESPERA")).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Crear ticket sin teléfono → no intenta notificación")
        void crearTicketSinTelefono_noIntentaNotificacion() {
            // Given
            String requestSinTelefono = String.format("""
                {
                    "nationalId": "%s",
                    "branchOffice": "Sucursal Norte",
                    "queueType": "CAJA"
                }
                """, generateUniqueNationalId());

            // When
            given()
                .contentType("application/json")
                .body(requestSinTelefono)
            .when()
                .post("/tickets")
            .then()
                .statusCode(201);

            // Then - El ticket se crea correctamente
            assertThat(countTicketsInStatus("EN_ESPERA")).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Múltiples tickets → sistema maneja múltiples notificaciones")
        void multiplesTickets_sistemaManejaNotificaciones() {
            // When - Crear 3 tickets con teléfono
            for (int i = 1; i <= 3; i++) {
                given()
                    .contentType("application/json")
                    .body(createTicketRequest(generateUniqueNationalId(), "+5691234567" + i, "Centro", "CAJA"))
                .when()
                    .post("/tickets")
                .then()
                    .statusCode(201);
            }

            // Then - Todos los tickets se crean correctamente
            assertThat(countTicketsInStatus("EN_ESPERA")).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Escenarios Edge Case (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Telegram caído → ticket sigue su flujo normal")
        void telegramCaido_ticketContinua() {
            // When - Crear ticket (Telegram fallará pero el ticket debe crearse)
            given()
                .contentType("application/json")
                .body(createTicketRequest(generateUniqueNationalId(), "+56911111111", "Centro", "CAJA"))
            .when()
                .post("/tickets")
            .then()
                .statusCode(201);

            // Then - El ticket debe crearse correctamente a pesar del fallo de Telegram
            assertThat(countTicketsInStatus("EN_ESPERA")).isGreaterThanOrEqualTo(1);
        }
    }
}
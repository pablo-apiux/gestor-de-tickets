package com.example.ticketero.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests using H2 in-memory database.
 * Simplified version without TestContainers for environments without Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTestSimple {

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void cleanDatabase() {
        // Limpiar en orden correcto (FK constraints)
        try {
            jdbcTemplate.execute("DELETE FROM ticket_event");
            jdbcTemplate.execute("DELETE FROM recovery_event");
            jdbcTemplate.execute("DELETE FROM outbox_message");
            jdbcTemplate.execute("DELETE FROM ticket");
            jdbcTemplate.execute("UPDATE advisor SET status = 'AVAILABLE', total_tickets_served = 0");
        } catch (Exception e) {
            // Ignorar errores si las tablas no existen aún
        }
    }

    // ============================================================
    // UTILITIES
    // ============================================================

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
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket WHERE status = ?",
                Integer.class, status);
        } catch (Exception e) {
            return 0;
        }
    }

    protected int countOutboxMessages(String status) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_message WHERE status = ?",
                Integer.class, status);
        } catch (Exception e) {
            return 0;
        }
    }

    protected int countAdvisorsInStatus(String status) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM advisor WHERE status = ?",
                Integer.class, status);
        } catch (Exception e) {
            return 0;
        }
    }

    protected void waitForTicketProcessing(int expectedCompleted, int timeoutSeconds) {
        org.awaitility.Awaitility.await()
            .atMost(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
            .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .until(() -> countTicketsInStatus("COMPLETED") >= expectedCompleted);
    }

    protected void setAdvisorStatus(Long advisorId, String status) {
        try {
            jdbcTemplate.update(
                "UPDATE advisor SET status = ? WHERE id = ?",
                status, advisorId);
        } catch (Exception e) {
            // Ignorar si no existe
        }
    }
}
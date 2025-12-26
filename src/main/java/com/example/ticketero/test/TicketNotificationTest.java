package com.example.ticketero.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TicketNotificationTest {
    
    private static final String API_URL = "http://localhost:8090";
    
    public static void main(String[] args) {
        System.out.println("🎫 PRUEBA DE NOTIFICACIONES DE TICKETS");
        System.out.println("======================================");
        
        crearTicketPrueba();
    }
    
    private static void crearTicketPrueba() {
        System.out.println("\n📝 Creando ticket de prueba...");
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
                    
            String json = """
                {
                    "nationalId": "99887766-5",
                    "telefono": "+56999888777",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """;
                
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/api/tickets"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
            
            if (response.statusCode() == 201) {
                System.out.println("✅ Ticket creado - Verifica tu Telegram!");
            } else {
                System.out.println("❌ Error creando ticket");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            System.out.println("💡 Asegúrate de que la API esté ejecutándose en puerto 8090");
        }
    }
}
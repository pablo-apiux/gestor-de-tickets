package com.example.ticketero.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DebugNotificationTest {
    
    private static final String API_URL = "http://localhost:8090";
    
    public static void main(String[] args) {
        System.out.println("🔍 DEBUG NOTIFICACIONES");
        System.out.println("=======================");
        
        // 1. Crear ticket con teléfono
        crearTicketConTelefono();
        
        // 2. Verificar mensajes en BD
        verificarMensajes();
    }
    
    private static void crearTicketConTelefono() {
        System.out.println("\n1️⃣ Creando ticket con teléfono...");
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
                    
            String json = """
                {
                    "nationalId": "87654321-0",
                    "telefono": "+56987654321",
                    "branchOffice": "Sucursal Debug",
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
            if (response.statusCode() == 201) {
                System.out.println("✅ Ticket creado");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("❌ Error: " + response.body());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void verificarMensajes() {
        System.out.println("\n2️⃣ Verificando logs de aplicación...");
        System.out.println("💡 Revisa la consola de la API para ver logs de TelegramService");
        System.out.println("💡 Busca mensajes como:");
        System.out.println("   - 'Error enviando notificación para ticket'");
        System.out.println("   - 'Error enviando mensaje a Telegram'");
    }
}
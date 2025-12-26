package com.example.ticketero.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramTest {
    
    private static final String BOT_TOKEN = "8581359386:AAGnLgS6OnTZheDaktIX10qrR-W4G01cj4Y";
    private static final String CHAT_ID = "55334007";
    private static final String API_URL = "https://api.telegram.org/bot";
    
    public static void main(String[] args) {
        System.out.println("🤖 PRUEBA DE TELEGRAM BOT");
        System.out.println("=========================");
        
        verificarBot();
        enviarMensajePrueba();
    }
    
    private static void verificarBot() {
        System.out.println("\n1️⃣ Verificando bot...");
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + BOT_TOKEN + "/getMe"))
                    .GET()
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private static void enviarMensajePrueba() {
        System.out.println("\n2️⃣ Enviando mensaje...");
        
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
                    
            String json = String.format("""
                {
                    "chat_id": "%s",
                    "text": "🧪 MENSAJE DE PRUEBA - Bot funcionando correctamente"
                }
                """, CHAT_ID);
                
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + BOT_TOKEN + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
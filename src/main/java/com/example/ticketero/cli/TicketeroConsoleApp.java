package com.example.ticketero.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class TicketeroConsoleApp {
    
    private static final String BASE_URL = "http://localhost:8090";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🎫 SISTEMA TICKETERO - INTERFAZ DE CONSOLA");
        System.out.println("==========================================");
        
        while (true) {
            mostrarMenu();
            int opcion = leerOpcion();
            
            switch (opcion) {
                case 1 -> crearTicket();
                case 2 -> listarTickets();
                case 3 -> buscarTicket();
                case 4 -> llamarTicket();
                case 5 -> finalizarTicket();
                case 6 -> verDashboard();
                case 7 -> verAsesores();
                case 8 -> verColas();
                case 0 -> {
                    System.out.println("¡Hasta luego!");
                    return;
                }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n📋 MENÚ PRINCIPAL:");
        System.out.println("1. 🆕 Crear Ticket");
        System.out.println("2. 📋 Listar Tickets Activos");
        System.out.println("3. 🔍 Buscar Ticket por Número");
        System.out.println("4. 📞 Llamar Ticket");
        System.out.println("5. ✅ Finalizar Ticket");
        System.out.println("6. 📊 Ver Dashboard");
        System.out.println("7. 👥 Ver Asesores");
        System.out.println("8. 🚶 Ver Estado de Colas");
        System.out.println("0. 🚪 Salir");
        System.out.print("\nSelecciona una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void crearTicket() {
        System.out.println("\n🆕 CREAR NUEVO TICKET");
        System.out.println("====================");
        
        System.out.print("RUT/Cédula: ");
        String nationalId = scanner.nextLine();
        
        System.out.print("Teléfono (opcional): ");
        String telefono = scanner.nextLine();
        
        System.out.print("Sucursal: ");
        String branchOffice = scanner.nextLine();
        
        System.out.println("\nTipos de Cola:");
        System.out.println("1. CAJA");
        System.out.println("2. PERSONAL_BANKER");
        System.out.println("3. EMPRESAS");
        System.out.println("4. GERENCIA");
        System.out.print("Selecciona tipo de cola (1-4): ");
        
        String queueType = switch (leerOpcion()) {
            case 1 -> "CAJA";
            case 2 -> "PERSONAL_BANKER";
            case 3 -> "EMPRESAS";
            case 4 -> "GERENCIA";
            default -> "CAJA";
        };

        String json = """
            {
                "nationalId": "%s",
                "telefono": "%s",
                "branchOffice": "%s",
                "queueType": "%s"
            }
            """.formatted(nationalId, telefono, branchOffice, queueType);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tickets"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                System.out.println("✅ Ticket creado exitosamente!");
                System.out.println(formatearJson(response.body()));
            } else {
                System.out.println("❌ Error creando ticket: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void listarTickets() {
        System.out.println("\n📋 TICKETS ACTIVOS");
        System.out.println("==================");
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tickets"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println(formatearJson(response.body()));
            } else {
                System.out.println("❌ Error obteniendo tickets: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void buscarTicket() {
        System.out.println("\n🔍 BUSCAR TICKET");
        System.out.println("================");
        
        System.out.print("Número de ticket: ");
        String numero = scanner.nextLine();
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tickets/" + numero))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println(formatearJson(response.body()));
            } else if (response.statusCode() == 404) {
                System.out.println("❌ Ticket no encontrado");
            } else {
                System.out.println("❌ Error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void llamarTicket() {
        System.out.println("\n📞 LLAMAR TICKET");
        System.out.println("================");
        
        System.out.print("ID del ticket: ");
        String ticketId = scanner.nextLine();
        
        System.out.print("ID del asesor: ");
        String advisorId = scanner.nextLine();
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tickets/" + ticketId + "/llamar/" + advisorId))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("✅ Ticket llamado exitosamente!");
            } else {
                System.out.println("❌ Error llamando ticket: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void finalizarTicket() {
        System.out.println("\n✅ FINALIZAR TICKET");
        System.out.println("===================");
        
        System.out.print("ID del ticket: ");
        String ticketId = scanner.nextLine();
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tickets/" + ticketId + "/finalizar"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("✅ Ticket finalizado exitosamente!");
            } else {
                System.out.println("❌ Error finalizando ticket: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verDashboard() {
        System.out.println("\n📊 DASHBOARD");
        System.out.println("============");
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/dashboard"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println(formatearJson(response.body()));
            } else {
                System.out.println("❌ Error obteniendo dashboard: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verAsesores() {
        System.out.println("\n👥 ASESORES");
        System.out.println("===========");
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/advisors"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println(formatearJson(response.body()));
            } else {
                System.out.println("❌ Error obteniendo asesores: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void verColas() {
        System.out.println("\n🚶 ESTADO DE COLAS");
        System.out.println("==================");
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/queue/status"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println(formatearJson(response.body()));
            } else {
                System.out.println("❌ Error obteniendo estado de colas: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static String formatearJson(String json) {
        try {
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            return json;
        }
    }
}
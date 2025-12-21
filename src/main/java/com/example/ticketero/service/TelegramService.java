package com.example.ticketero.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final RestTemplate restTemplate;

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.api-url}")
    private String apiUrl;

    public String enviarMensaje(String chatId, String texto) {
        String url = apiUrl + botToken + "/sendMessage";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "chat_id", chatId,
            "text", texto,
            "parse_mode", "HTML"
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                return result.get("message_id").toString();
            }
            
            throw new RuntimeException("Error enviando mensaje a Telegram");
            
        } catch (Exception e) {
            log.error("Error enviando mensaje a Telegram: {}", e.getMessage());
            throw new RuntimeException("Error enviando mensaje a Telegram: " + e.getMessage());
        }
    }

    public String obtenerTextoMensaje(String plantilla, String numeroTicket, Integer posicion, Integer tiempoEstimado, String nombreAsesor, Integer modulo) {
        return switch (plantilla) {
            case "totem_ticket_creado" -> String.format(
                "✅ <b>Ticket Creado</b>\n\nTu número de turno: <b>%s</b>\nPosición en cola: <b>#%d</b>\nTiempo estimado: <b>%d minutos</b>\n\nTe notificaremos cuando estés próximo.",
                numeroTicket, posicion, tiempoEstimado
            );
            case "totem_proximo_turno" -> String.format(
                "⏰ <b>¡Pronto será tu turno!</b>\n\nTurno: <b>%s</b>\nFaltan aproximadamente 3 turnos.\n\nPor favor, acércate a la sucursal.",
                numeroTicket
            );
            case "totem_es_tu_turno" -> String.format(
                "🔔 <b>¡ES TU TURNO %s!</b>\n\nDirígete al módulo: <b>%d</b>\nAsesor: <b>%s</b>",
                numeroTicket, modulo, nombreAsesor
            );
            default -> throw new IllegalArgumentException("Plantilla no válida: " + plantilla);
        };
    }
}
package com.example.ticketero.controller;

import com.example.ticketero.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final TelegramService telegramService;

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    @GetMapping("/telegram-config")
    public Map<String, Object> getTelegramConfig() {
        return Map.of(
            "botTokenConfigured", botToken != null && !botToken.isEmpty(),
            "chatIdConfigured", chatId != null && !chatId.isEmpty(),
            "botTokenLength", botToken != null ? botToken.length() : 0,
            "chatId", chatId != null ? chatId : "null"
        );
    }

    @GetMapping("/test-notification")
    public Map<String, Object> testNotification() {
        try {
            String texto = telegramService.obtenerTextoMensaje(
                "totem_ticket_creado",
                "DEBUG001",
                1,
                5,
                null,
                null
            );
            
            String messageId = telegramService.enviarMensaje("+56999999999", texto);
            
            return Map.of(
                "success", true,
                "messageId", messageId,
                "texto", texto
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}
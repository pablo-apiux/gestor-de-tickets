package com.example.ticketero.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelegramService - Unit Tests")
class TelegramServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TelegramService telegramService;

    // ============================================================
    // ENVIAR MENSAJE
    // ============================================================
    
    @Nested
    @DisplayName("enviarMensaje()")
    class EnviarMensaje {

        @Test
        @DisplayName("con token configurado y respuesta exitosa → debe retornar message_id")
        void enviarMensaje_conTokenYRespuestaExitosa_debeRetornarMessageId() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            Map<String, Object> responseBody = Map.of(
                "ok", true,
                "result", Map.of("message_id", 12345)
            );
            ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
            
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

            // When
            String resultado = telegramService.enviarMensaje("+56912345678", "Test message");

            // Then
            assertThat(resultado).isEqualTo("12345");
            verify(restTemplate).postForEntity(
                contains("sendMessage"),
                any(HttpEntity.class),
                eq(Map.class)
            );
        }

        @Test
        @DisplayName("sin token configurado → debe simular envío")
        void enviarMensaje_sinToken_debeSimularEnvio() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "");

            // When
            String resultado = telegramService.enviarMensaje("+56912345678", "Test message");

            // Then
            assertThat(resultado).startsWith("simulated_message_id_");
            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        }

        @Test
        @DisplayName("con error de API → debe lanzar RuntimeException")
        void enviarMensaje_conErrorApi_debeLanzarExcepcion() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

            // When + Then
            assertThatThrownBy(() -> telegramService.enviarMensaje("+56912345678", "Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error enviando mensaje a Telegram");
        }

        @Test
        @DisplayName("con respuesta sin result → debe lanzar RuntimeException")
        void enviarMensaje_conRespuestaSinResult_debeLanzarExcepcion() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            ResponseEntity<Map> response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

            // When + Then
            assertThatThrownBy(() -> telegramService.enviarMensaje("+56912345678", "Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error enviando mensaje a Telegram");
        }
    }

    // ============================================================
    // OBTENER TEXTO MENSAJE
    // ============================================================
    
    @Nested
    @DisplayName("obtenerTextoMensaje()")
    class ObtenerTextoMensaje {

        @Test
        @DisplayName("plantilla ticket_creado → debe generar mensaje correcto")
        void obtenerTexto_plantillaTicketCreado_debeGenerarMensaje() {
            // When
            String resultado = telegramService.obtenerTextoMensaje(
                "totem_ticket_creado", "C001", 3, 15, null, null
            );

            // Then
            assertThat(resultado)
                .contains("Ticket Creado")
                .contains("C001")
                .contains("#3")
                .contains("15 minutos");
        }

        @Test
        @DisplayName("plantilla proximo_turno → debe generar mensaje correcto")
        void obtenerTexto_plantillaProximoTurno_debeGenerarMensaje() {
            // When
            String resultado = telegramService.obtenerTextoMensaje(
                "totem_proximo_turno", "C001", null, null, null, null
            );

            // Then
            assertThat(resultado)
                .contains("Pronto será tu turno")
                .contains("C001")
                .contains("3 turnos");
        }

        @Test
        @DisplayName("plantilla es_tu_turno → debe generar mensaje correcto")
        void obtenerTexto_plantillaEsTuTurno_debeGenerarMensaje() {
            // When
            String resultado = telegramService.obtenerTextoMensaje(
                "totem_es_tu_turno", "C001", null, null, "María López", 3
            );

            // Then
            assertThat(resultado)
                .contains("ES TU TURNO C001")
                .contains("módulo:")
                .contains("3")
                .contains("María López");
        }

        @Test
        @DisplayName("plantilla inválida → debe lanzar IllegalArgumentException")
        void obtenerTexto_plantillaInvalida_debeLanzarExcepcion() {
            // When + Then
            assertThatThrownBy(() -> telegramService.obtenerTextoMensaje(
                "plantilla_inexistente", "C001", null, null, null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plantilla no válida: plantilla_inexistente");
        }

        @Test
        @DisplayName("debe manejar valores null correctamente")
        void obtenerTexto_conValoresNull_debeManejarCorrectamente() {
            // When
            String resultado = telegramService.obtenerTextoMensaje(
                "totem_ticket_creado", "C001", 1, 5, null, null
            );

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).contains("C001");
        }
    }

    // ============================================================
    // CASOS EDGE ADICIONALES PARA COBERTURA COMPLETA
    // ============================================================
    
    @Nested
    @DisplayName("Casos Edge para Cobertura Completa")
    class CasosEdge {

        @Test
        @DisplayName("enviarMensaje con respuesta exitosa pero sin message_id → debe retornar unknown_message_id")
        void enviarMensaje_respuestaExitosaSinMessageId_debeRetornarUnknown() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            Map<String, Object> responseBody = Map.of(
                "ok", true,
                "result", Map.of("other_field", "value") // Sin message_id
            );
            ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
            
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

            // When
            String resultado = telegramService.enviarMensaje("+56912345678", "Test message");

            // Then
            assertThat(resultado).isEqualTo("unknown_message_id");
        }

        @Test
        @DisplayName("enviarMensaje con respuesta exitosa pero result no es Map → debe lanzar excepción")
        void enviarMensaje_resultNoEsMap_debeLanzarExcepcion() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            Map<String, Object> responseBody = Map.of(
                "ok", true,
                "result", "not_a_map" // result no es Map
            );
            ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
            
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

            // When + Then
            assertThatThrownBy(() -> telegramService.enviarMensaje("+56912345678", "Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error enviando mensaje a Telegram");
        }

        @Test
        @DisplayName("enviarMensaje con token null → debe simular envío")
        void enviarMensaje_conTokenNull_debeSimularEnvio() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", null);

            // When
            String resultado = telegramService.enviarMensaje("+56912345678", "Test message");

            // Then
            assertThat(resultado).startsWith("simulated_message_id_");
            verify(restTemplate, never()).postForEntity(anyString(), any(), any());
        }

        @Test
        @DisplayName("enviarMensaje con respuesta sin body → debe lanzar excepción")
        void enviarMensaje_respuestaSinBody_debeLanzarExcepcion() {
            // Given
            ReflectionTestUtils.setField(telegramService, "botToken", "123456:ABC-DEF");
            ReflectionTestUtils.setField(telegramService, "apiUrl", "https://api.telegram.org/bot");
            ReflectionTestUtils.setField(telegramService, "chatId", "12345");
            
            ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.OK); // Sin body
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

            // When + Then
            assertThatThrownBy(() -> telegramService.enviarMensaje("+56912345678", "Test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error enviando mensaje a Telegram");
        }
    }
}
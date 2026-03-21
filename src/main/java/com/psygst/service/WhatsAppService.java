package com.psygst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${whatsapp.provider:none}")
    private String provider;

    @Value("${whatsapp.ultramsg.instance-id:}")
    private String instanceId;

    @Value("${whatsapp.ultramsg.token:}")
    private String token;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void enviar(String celular, String mensaje) {
        if ("none".equals(provider) || instanceId.isBlank() || token.isBlank()) {
            log.info("[WHATSAPP STUB] To: {} | Msg: {}", celular, mensaje);
            return;
        }

        if ("ultramsg".equals(provider)) {
            enviarUltraMsg(celular, mensaje);
        } else {
            log.warn("WhatsApp provider '{}' no implementado", provider);
        }
    }

    private void enviarUltraMsg(String celular, String mensaje) {
        try {
            String url = "https://api.ultramsg.com/" + instanceId + "/messages/chat";
            String body = "token=" + token + "&to=" + celular + "&body=" + java.net.URLEncoder.encode(mensaje, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("UltraMsg response [{}]: {}", response.statusCode(), response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("UltraMsg error " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            log.error("Error enviando WhatsApp a {}: {}", celular, e.getMessage());
            throw new RuntimeException("Error WhatsApp: " + e.getMessage(), e);
        }
    }
}

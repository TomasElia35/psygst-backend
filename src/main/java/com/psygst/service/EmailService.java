/*package com.psygst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from-name:PsyGst}")
    private String fromName;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void enviar(String to, String subject, String body) {
        if (mailSender == null || fromEmail.isBlank()) {
            log.info("[EMAIL STUB] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Email enviado a {}: {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    public void enviarConAdjunto(String to, String subject, String body, byte[] pdf, String filename) {
        if (mailSender == null || fromEmail.isBlank()) {
            log.info("[EMAIL STUB] Adjunto: {} | To: {}", filename, to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(filename, new org.springframework.core.io.ByteArrayResource(pdf));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando email con adjunto: {}", e.getMessage());
            throw new RuntimeException("Error enviando email con adjunto", e);
        }
    }
}
*/
package com.psygst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api-key:}")
    private String resendApiKey;

    // IMPORTANTE: Hasta que valides un dominio propio en Resend, 
    // debes usar este remitente de prueba por defecto.
    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${resend.from-name:PsyGst Notificaciones}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String RESEND_API_URL = "https://api.resend.com/emails";

    public void enviar(String to, String subject, String body) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.info("[EMAIL STUB] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }

        try {
            // 1. Configurar las cabeceras de la petición web
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            // 2. Armar el cuerpo del JSON que exige Resend
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromName + " <" + fromEmail + ">");
            requestBody.put("to", List.of(to));
            requestBody.put("subject", subject);
            requestBody.put("html", body); // Usamos 'html' para aceptar formato

            // 3. Enviar la petición POST
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(RESEND_API_URL, request, String.class);
            
            log.info("✅ Email enviado a través de Resend a {}: {}", to, subject);

        } catch (Exception e) {
            log.error("❌ Error enviando email con Resend a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    public void enviarConAdjunto(String to, String subject, String body, byte[] pdf, String filename) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.info("[EMAIL STUB] Adjunto: {} | To: {}", filename, to);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromName + " <" + fromEmail + ">");
            requestBody.put("to", List.of(to));
            requestBody.put("subject", subject);
            requestBody.put("html", body);

            // Convertir el byte[] del PDF a un String en formato Base64
            String base64Content = Base64.getEncoder().encodeToString(pdf);
            
            // Armar el objeto del adjunto
            Map<String, String> attachment = new HashMap<>();
            attachment.put("filename", filename);
            attachment.put("content", base64Content);
            
            requestBody.put("attachments", List.of(attachment));

            // Enviar petición
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(RESEND_API_URL, request, String.class);
            
            log.info("✅ Email con adjunto enviado a través de Resend a {}: {}", to, subject);

        } catch (Exception e) {
            log.error("❌ Error enviando email con adjunto por Resend: {}", e.getMessage());
            throw new RuntimeException("Error enviando email con adjunto", e);
        }
    }
}

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
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    // Ahora leemos la URL del script de Google y el token desde el properties
    @Value("${google.script.url:}")
    private String googleScriptUrl;

    @Value("${google.script.token:PsyGst_Token_987654321}")
    private String scriptToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviar(String to, String subject, String body) {
        if (googleScriptUrl == null || googleScriptUrl.isBlank()) {
            log.info("[EMAIL STUB] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", to);
            requestBody.put("subject", subject);
            requestBody.put("body", body);
            requestBody.put("token", scriptToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(googleScriptUrl, request, String.class);
            
            log.info("✅ Email enviado a través de Google Script a {}: {}", to, subject);

        } catch (Exception e) {
            log.error("❌ Error enviando email con Google Script a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    public void enviarConAdjunto(String to, String subject, String body, byte[] pdf, String filename) {
        if (googleScriptUrl == null || googleScriptUrl.isBlank()) {
            log.info("[EMAIL STUB] Adjunto: {} | To: {}", filename, to);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", to);
            requestBody.put("subject", subject);
            requestBody.put("body", body);
            requestBody.put("token", scriptToken);

            // Convertir el byte[] del PDF a Base64 y enviarlo al script
            String base64Content = Base64.getEncoder().encodeToString(pdf);
            requestBody.put("attachmentBase64", base64Content);
            requestBody.put("filename", filename);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(googleScriptUrl, request, String.class);
            
            log.info("✅ Email con adjunto enviado a través de Google Script a {}: {}", to, subject);

        } catch (Exception e) {
            log.error("❌ Error enviando email con adjunto por Google Script: {}", e.getMessage());
            throw new RuntimeException("Error enviando email con adjunto", e);
        }
    }
}

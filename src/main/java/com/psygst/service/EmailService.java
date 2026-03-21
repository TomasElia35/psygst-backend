package com.psygst.service;

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

package com.psygst.service;

import com.psygst.exception.*;
import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.notificacion.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    /** RN-N01: schedule reminder 24h before turno */
    @Transactional
    public void programarRecordatorio(Turno turno) {
        LocalDateTime fechaProgramada = turno.getFecha().atTime(turno.getHoraComienzo()).minusHours(24);

        // Create WhatsApp notification if patient has phone
        if (turno.getPaciente().getCelular() != null && !turno.getPaciente().getCelular().isBlank()) {
            crearNotificacion(turno, "RECORDATORIO_24HS", "WHATSAPP", fechaProgramada);
        }

        // Create Email notification if patient has email
        if (turno.getPaciente().getEmail() != null && !turno.getPaciente().getEmail().isBlank()) {
            crearNotificacion(turno, "RECORDATORIO_24HS", "EMAIL", fechaProgramada);
        }
    }

    /**
     * Bug-6 fix: Immediately sends a WhatsApp booking confirmation.
     * This avoids the 15-minute wait by executing the send logic synchronously right away.
     */
    @Transactional
    public void enviarConfirmacionInmediata(Turno turno) {
        if (turno.getPaciente().getCelular() != null && !turno.getPaciente().getCelular().isBlank()) {
            Notificacion n = crearNotificacion(turno, "CONFIRMACION_TURNO", "WHATSAPP", LocalDateTime.now());
            try {
                enviarNotificacion(n);
                n.setEstado("ENVIADO");
                n.setFechaEnvio(LocalDateTime.now());
                n.setIntentos(1);
            } catch (Exception e) {
                n.setIntentos(1);
                n.setDetalle("Error inmediato: " + e.getMessage());
                log.warn("Error enviando WhatsApp inmediato a turno {}: {}", turno.getUuid(), e.getMessage());
            }
            notificacionRepository.save(n);
            log.info("Confirmación WhatsApp procesada inmediatamente para turno {}", turno.getUuid());
        }
        
        if (turno.getPaciente().getEmail() != null && !turno.getPaciente().getEmail().isBlank()) {
            Notificacion ne = crearNotificacion(turno, "CONFIRMACION_TURNO", "EMAIL", LocalDateTime.now());
            try {
                enviarNotificacion(ne);
                ne.setEstado("ENVIADO");
                ne.setFechaEnvio(LocalDateTime.now());
                ne.setIntentos(1);
            } catch (Exception e) {
                ne.setIntentos(1);
                ne.setDetalle("Error inmediato: " + e.getMessage());
                log.warn("Error enviando Email inmediato a turno {}: {}", turno.getUuid(), e.getMessage());
            }
            notificacionRepository.save(ne);
            log.info("Confirmación Email procesada inmediatamente para turno {}", turno.getUuid());
        }
    }

    /** RN-N03: on cancel, void reminders and create cancellation notification */
    @Transactional
    public void procesarCancelacion(Turno turno) {
        // Anular pending reminders
        List<Notificacion> reminders = notificacionRepository.findPendingRemindersByTurno(turno.getIdTurno());
        reminders.forEach(n -> {
            n.setEstado("CANCELADO");
            notificacionRepository.save(n);
        });

        // Create cancellation notifications and send them immediately
        if (turno.getPaciente().getCelular() != null && !turno.getPaciente().getCelular().isBlank()) {
            Notificacion n = crearNotificacion(turno, "CANCELACION", "WHATSAPP", LocalDateTime.now());
            try {
                enviarNotificacion(n);
                n.setEstado("ENVIADO");
                n.setFechaEnvio(LocalDateTime.now());
                n.setIntentos(1);
            } catch (Exception e) {
                n.setIntentos(1);
                n.setDetalle("Error inmediato: " + e.getMessage());
                log.warn("Error enviando WhatsApp cancelación: {}", e.getMessage());
            }
            notificacionRepository.save(n);
        }
        if (turno.getPaciente().getEmail() != null && !turno.getPaciente().getEmail().isBlank()) {
            Notificacion ne = crearNotificacion(turno, "CANCELACION", "EMAIL", LocalDateTime.now());
            try {
                enviarNotificacion(ne);
                ne.setEstado("ENVIADO");
                ne.setFechaEnvio(LocalDateTime.now());
                ne.setIntentos(1);
            } catch (Exception e) {
                ne.setIntentos(1);
                ne.setDetalle("Error inmediato: " + e.getMessage());
                log.warn("Error enviando Email cancelación: {}", e.getMessage());
            }
            notificacionRepository.save(ne);
        }
    }

    private Notificacion crearNotificacion(Turno turno, String tipo, String canal, LocalDateTime fechaProgramada) {
        Notificacion n = Notificacion.builder()
                .uuid(UUID.randomUUID().toString())
                .turno(turno)
                .paciente(turno.getPaciente())
                .tipo(tipo)
                .canal(canal)
                .estado("PENDIENTE")
                .intentos(0)
                .fechaProgramada(fechaProgramada)
                .profesional(turno.getProfesional())
                .sistema(turno.getSistema())
                .baja((byte) 0)
                .build();
        return notificacionRepository.save(n);
    }

    /**
     * RN-N02: Scheduler job every 15 minutes.
     * Sends due notifications, max 3 retries before marking as FALLIDO.
     */
    @Scheduled(fixedDelay = 900000) // 15 minutes
    @Transactional
    public void procesarNotificacionesPendientes() {
        List<Notificacion> pendientes = notificacionRepository.findDueNotificaciones(LocalDateTime.now());
        log.info("Scheduler: procesando {} notificaciones pendientes", pendientes.size());

        for (Notificacion n : pendientes) {
            // RN-N04: verify turno still CONFIRMADO for RECORDATORIO_24HS
            if ("RECORDATORIO_24HS".equals(n.getTipo())) {
                if (!"CONFIRMADO".equals(n.getTurno().getEstado())) {
                    n.setEstado("CANCELADO");
                    n.setDetalle("Turno ya no está confirmado");
                    notificacionRepository.save(n);
                    continue;
                }
            }

            try {
                enviarNotificacion(n);
                n.setEstado("ENVIADO");
                n.setFechaEnvio(LocalDateTime.now());
                n.setIntentos(n.getIntentos() + 1);
            } catch (Exception e) {
                n.setIntentos(n.getIntentos() + 1);
                n.setDetalle("Error: " + e.getMessage());
                log.warn("Error enviando notificación {}: {}", n.getUuid(), e.getMessage());

                // RN-N02: max 3 attempts
                if (n.getIntentos() >= 3) {
                    n.setEstado("FALLIDO");
                    log.warn("Notificación {} marcada como FALLIDA tras 3 intentos", n.getUuid());
                }
            }
            notificacionRepository.save(n);
        }
    }

    private void enviarNotificacion(Notificacion n) {
        String mensaje = buildMensaje(n);
        if ("EMAIL".equals(n.getCanal())) {
            emailService.enviar(n.getPaciente().getEmail(), buildAsunto(n), mensaje);
        } else if ("WHATSAPP".equals(n.getCanal())) {
            whatsAppService.enviar(n.getPaciente().getCelular(), mensaje);
        }
    }

    private String buildMensaje(Notificacion n) {
        Turno turno = n.getTurno();
        String paciente = turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido();
        String profNombreCompleto = turno.getProfesional().getApellido() + " " + turno.getProfesional().getNombre();
        return switch (n.getTipo()) {
            case "CONFIRMACION_TURNO" -> String.format(
                    "✅ Hola %s! Su turno fue confirmado para el %s a las %s. Modalidad: %s. ¡Nos vemos!\nProfesional: %s",
                    turno.getPaciente().getNombre(), turno.getFecha(), turno.getHoraComienzo(), turno.getModalidad(), profNombreCompleto);
            case "RECORDATORIO_24HS" -> String.format(
                    "Hola %s! Le recordamos su sesión de mañana %s a las %s. Modalidad: %s.",
                    turno.getPaciente().getNombre(), turno.getFecha(), turno.getHoraComienzo(), turno.getModalidad());
            case "CANCELACION" -> String.format(
                    "Hola %s! Su turno del %s a las %s ha sido cancelado. Comuníquese para reprogramar.",
                    turno.getPaciente().getNombre(), turno.getFecha(), turno.getHoraComienzo());
            case "DATOS_PAGO" -> buildMensajePago(turno);
            default -> "Notificación de PsyGst";
        };
    }

    private String buildMensajePago(Turno turno) {
        Profesional prof = turno.getProfesional();
        return String.format(
                "Hola %s! Para abonar su sesión del %s, transferí $%.2f a:\nCBU: %s\nAlias: %s",
                turno.getPaciente().getNombre(), turno.getFecha(), turno.getPrecioFinal(),
                prof.getCbu() != null ? prof.getCbu() : "(consultar)",
                prof.getAlias() != null ? prof.getAlias() : "(consultar)");
    }

    private String buildAsunto(Notificacion n) {
        return switch (n.getTipo()) {
            case "CONFIRMACION_TURNO" -> "Turno confirmado - PsyGst";
            case "RECORDATORIO_24HS" -> "Recordatorio de tu sesión - PsyGst";
            case "CANCELACION" -> "Turno cancelado - PsyGst";
            case "DATOS_PAGO" -> "Datos de pago - PsyGst";
            default -> "Notificación - PsyGst";
        };
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerFallidas() {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        return notificacionRepository
                .findByProfesional_IdProfesionalAndEstadoAndBaja(idProfesional, "FALLIDO", (byte) 0)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerPorTurno(String turnoUuid) {
        // Lookup through turno
        return notificacionRepository.findAll().stream()
                .filter(n -> n.getTurno().getUuid().equals(turnoUuid) && n.getBaja() == 0)
                .map(this::toResponse).collect(Collectors.toList());
    }

    /** RN-N02: Manual resend — reset intentos to 0, back to PENDIENTE */
    @Transactional
    public void reenviar(String uuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Notificacion n = notificacionRepository.findByUuidAndSistema_IdSistemaAndBaja(uuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));
        n.setEstado("PENDIENTE");
        n.setIntentos(0);
        n.setFechaProgramada(LocalDateTime.now());
        n.setDetalle(null);
        notificacionRepository.save(n);
    }

    @Transactional
    public void enviarDatosPago(String turnoUuid) {
        // Look up turno and create a DATOS_PAGO notification immediately
        // This is triggered manually (CU-14)
        notificacionRepository.findAll().stream()
                .filter(n -> n.getTurno().getUuid().equals(turnoUuid) && n.getBaja() == 0)
                .findFirst()
                .ifPresent(n -> {
                    crearNotificacion(n.getTurno(), "DATOS_PAGO", "WHATSAPP", LocalDateTime.now());
                });
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
                n.getUuid(),
                n.getTipo(),
                n.getCanal(),
                n.getEstado(),
                n.getIntentos(),
                n.getDetalle(),
                n.getFechaProgramada(),
                n.getFechaEnvio(),
                n.getTurno() != null ? n.getTurno().getUuid() : null,
                n.getPaciente() != null ? n.getPaciente().getNombre() + " " + n.getPaciente().getApellido() : null);
    }
}

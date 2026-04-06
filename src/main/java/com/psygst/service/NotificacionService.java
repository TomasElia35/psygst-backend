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

import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    // Formateador de fecha: martes 28-03-2026
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE dd-MM-yyyy",
            new Locale("es", "ES"));

    @Transactional
    public void programarRecordatorio(Turno turno) {
        LocalDateTime fechaProgramada = turno.getFecha().atTime(turno.getHoraComienzo()).minusHours(24);
        if (turno.getPaciente().getCelular() != null && !turno.getPaciente().getCelular().isBlank()) {
            crearNotificacion(turno, "RECORDATORIO_24HS", "WHATSAPP", fechaProgramada);
        }
        if (turno.getPaciente().getEmail() != null && !turno.getPaciente().getEmail().isBlank()) {
            crearNotificacion(turno, "RECORDATORIO_24HS", "EMAIL", fechaProgramada);
        }
    }

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
            }
            notificacionRepository.save(n);
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
            }
            notificacionRepository.save(ne);
        }
    }

    @Transactional
    public void procesarCancelacion(Turno turno) {
        List<Notificacion> reminders = notificacionRepository.findPendingRemindersByTurno(turno.getIdTurno());
        reminders.forEach(n -> {
            n.setEstado("CANCELADO");
            notificacionRepository.save(n);
        });

        if (turno.getPaciente().getCelular() != null && !turno.getPaciente().getCelular().isBlank()) {
            Notificacion n = crearNotificacion(turno, "CANCELACION", "WHATSAPP", LocalDateTime.now());
            try {
                enviarNotificacion(n);
                n.setEstado("ENVIADO");
                n.setFechaEnvio(LocalDateTime.now());
                n.setIntentos(1);
            } catch (Exception e) {
                n.setIntentos(1);
                n.setDetalle("Error: " + e.getMessage());
            }
            notificacionRepository.save(n);
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

    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void procesarNotificacionesPendientes() {
        List<Notificacion> pendientes = notificacionRepository.findDueNotificaciones(LocalDateTime.now());
        for (Notificacion n : pendientes) {
            if ("RECORDATORIO_24HS".equals(n.getTipo()) && !"CONFIRMADO".equals(n.getTurno().getEstado())) {
                n.setEstado("CANCELADO");
                notificacionRepository.save(n);
                continue;
            }
            try {
                enviarNotificacion(n);
                n.setEstado("ENVIADO");
                n.setFechaEnvio(LocalDateTime.now());
                n.setIntentos(n.getIntentos() + 1);
            } catch (Exception e) {
                n.setIntentos(n.getIntentos() + 1);
                n.setDetalle("Error: " + e.getMessage());
                if (n.getIntentos() >= 3)
                    n.setEstado("FALLIDO");
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
        String pacienteNombre = turno.getPaciente().getNombre();
        String profNombre = turno.getProfesional().getApellido() + " " + turno.getProfesional().getNombre();

        // Formateo con día de la semana
        String fechaFormateada = turno.getFecha().format(dateFormatter);
        // Capitalizamos el día (ej: Martes)
        fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
        String horaFormateada = turno.getHoraComienzo().toString() + "hs";

        return switch (n.getTipo()) {
            case "CONFIRMACION_TURNO" -> String.format(
                    "✅ Hola %s! Su turno fue confirmado para el %s a las %s. Modalidad: %s. ¡Nos vemos!\nProfesional: %s",
                    pacienteNombre, fechaFormateada, horaFormateada, turno.getModalidad(), profNombre);

            case "RECORDATORIO_24HS" -> String.format(
                    "Hola %s! Le recordamos su sesión de mañana %s a las %s. Modalidad: %s.",
                    pacienteNombre, fechaFormateada, horaFormateada, turno.getModalidad());

            case "CANCELACION" -> String.format(
                    "Hola %s! Su turno del %s a las %s ha sido cancelado. Comuníquese para reprogramar.",
                    pacienteNombre, fechaFormateada, horaFormateada);

            case "DATOS_PAGO" -> buildMensajePago(turno);
            default -> "Notificación de PsyGst";
        };
    }

    private String buildMensajePago(Turno turno) {
        Profesional prof = turno.getProfesional();
        String fechaFormateada = turno.getFecha().format(dateFormatter);
        fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);

        return String.format(
                "Hola %s! Para abonar su sesión del %s, transferí $%.2f a:\nCBU: %s\nAlias: %s",
                turno.getPaciente().getNombre(), fechaFormateada, turno.getPrecioFinal(),
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
        return notificacionRepository.findAll().stream()
                .filter(n -> n.getTurno().getUuid().equals(turnoUuid) && n.getBaja() == 0)
                .map(this::toResponse).collect(Collectors.toList());
    }

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
        notificacionRepository.findAll().stream()
                .filter(n -> n.getTurno().getUuid().equals(turnoUuid) && n.getBaja() == 0)
                .findFirst()
                .ifPresent(n -> crearNotificacion(n.getTurno(), "DATOS_PAGO", "WHATSAPP", LocalDateTime.now()));
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
                n.getUuid(), n.getTipo(), n.getCanal(), n.getEstado(),
                n.getIntentos(), n.getDetalle(), n.getFechaProgramada(), n.getFechaEnvio(),
                n.getTurno() != null ? n.getTurno().getUuid() : null,
                n.getPaciente() != null ? n.getPaciente().getNombre() + " " + n.getPaciente().getApellido() : null);
    }
}
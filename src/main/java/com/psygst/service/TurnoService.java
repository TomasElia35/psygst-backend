package com.psygst.service;

import com.psygst.exception.*;
import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.turno.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final SistemaRepository sistemaRepository;
    private final PagoService pagoService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<TurnoResponse> obtenerSemana(LocalDate fechaInicio) {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        LocalDate fechaFin = fechaInicio.plusDays(6);
        return turnoRepository.findByProfesionalAndWeek(idProfesional, fechaInicio, fechaFin)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TurnoResponse obtener(String id) {
        return toResponse(findById(id));
    }

    @Transactional
    public TurnoResponse crear(TurnoRequest request) {
        String idSistema     = SecurityContextUtil.getCurrentIdSistema();
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        // RN-T02: HoraFin > HoraComienzo
        if (!request.horaFin().isAfter(request.horaComienzo())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // RN-T05: no past dates
        if (request.fecha().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se pueden crear turnos con fecha pasada");
        }

        // RN-T01: overlap check (no excludeId = null for new turno)
        boolean overlap = turnoRepository.existsOverlap(
                idProfesional, request.fecha(), request.horaComienzo(), request.horaFin(), null);
        if (overlap) {
            throw new ConflictException("Conflicto de horario: ya existe un turno activo en ese rango horario");
        }

        Paciente paciente = pacienteRepository.findByIdPacienteAndSistema_IdSistemaAndBaja(
                request.pacienteUuid(), idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));

        Sistema sistema = sistemaRepository.findById(idSistema)
                .orElseThrow(() -> new BadRequestException("Sistema no encontrado"));

        ObraSocial obraSocial = paciente.getObraSocial();

        // RN-T06: freeze price at creation time
        Turno turno = Turno.builder()
                .paciente(paciente)
                .profesional(profesional)
                .sistema(sistema)
                .fecha(request.fecha())
                .horaComienzo(request.horaComienzo())
                .horaFin(request.horaFin())
                .modalidad(request.modalidad())
                .estado("CONFIRMADO")
                .precioFinal(request.precioFinal())
                .obraSocial(obraSocial)
                .observaciones(request.observaciones())
                .baja(false)
                .build();
        // idTurno generated in @PrePersist

        turno = turnoRepository.save(turno);

        // RN-F03: auto-create pending payment
        pagoService.crearPagoParaTurno(turno);

        // RN-N01: schedule 24h reminder
        notificacionService.programarRecordatorio(turno);

        // Bug-6 fix: immediately queue WhatsApp booking confirmation
        notificacionService.enviarConfirmacionInmediata(turno);

        return toResponse(turno);
    }

    @Transactional
    public TurnoResponse cambiarEstado(String id, String nuevoEstado) {
        Turno turno = findById(id);

        // RN-T04: REALIZADO is terminal
        if ("REALIZADO".equals(turno.getEstado())) {
            throw new BadRequestException("Un turno realizado no puede cambiar de estado");
        }

        if (!"CANCELADO".equals(nuevoEstado) && !"REALIZADO".equals(nuevoEstado)) {
            throw new BadRequestException("Estado invÃ¡lido. Valores permitidos: CANCELADO, REALIZADO");
        }

        turno.setEstado(nuevoEstado);
        turno = turnoRepository.save(turno);

        // RN-N03: on cancel, notify patient and void pending reminders
        if ("CANCELADO".equals(nuevoEstado)) {
            notificacionService.procesarCancelacion(turno);

            // RN-Payment 48h: if cancelled more than 48hs before, annul payment
            LocalDateTime turnoInicio = turno.getFecha().atTime(turno.getHoraComienzo());
            long hoursDiff = java.time.Duration.between(LocalDateTime.now(), turnoInicio).toHours();
            if (hoursDiff >= 48) {
                pagoService.anularPagoDeTurnoPorId(turno.getIdTurno());
            }
        }

        return toResponse(turno);
    }

    @Transactional
    public TurnoResponse actualizar(String id, TurnoRequest request) {
        Turno turno = findById(id);
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        if ("REALIZADO".equals(turno.getEstado())) {
            throw new BadRequestException("No se puede modificar un turno realizado");
        }

        if (!request.horaFin().isAfter(request.horaComienzo())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // RN-T01: overlap check excluding self
        boolean overlap = turnoRepository.existsOverlap(
                idProfesional, request.fecha(), request.horaComienzo(), request.horaFin(), turno.getIdTurno());
        if (overlap) {
            throw new ConflictException("Conflicto de horario: ya existe un turno activo en ese rango horario");
        }

        turno.setFecha(request.fecha());
        turno.setHoraComienzo(request.horaComienzo());
        turno.setHoraFin(request.horaFin());
        turno.setModalidad(request.modalidad());
        turno.setObservaciones(request.observaciones());

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public void eliminar(String id) {
        Turno turno = findById(id);
        turno.setBaja(true);
        turnoRepository.save(turno);
    }

    private Turno findById(String id) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        return turnoRepository.findByIdTurnoAndSistema_IdSistemaAndBaja(id, idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado"));
    }

    public TurnoResponse toResponse(Turno t) {
        return new TurnoResponse(
                t.getIdTurno(),                    // UUID PK
                t.getPaciente().getIdPaciente(),   // UUID PK
                t.getPaciente().getNombre() + " " + t.getPaciente().getApellido(),
                t.getFecha(),
                t.getHoraComienzo(),
                t.getHoraFin(),
                t.getModalidad(),
                t.getEstado(),
                t.getPrecioFinal(),
                t.getObservaciones());
    }
}

package com.psygst.service;

import com.psygst.exception.*;
import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.paciente.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final MotivoRepository motivoRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final SistemaRepository sistemaRepository;
    private final ProfesionalRepository profesionalRepository;

    @Transactional(readOnly = true)
    public Page<PacienteResponse> listar(int page, int size, String q) {
        String idSistema      = SecurityContextUtil.getCurrentIdSistema();
        String idProfesional  = SecurityContextUtil.getCurrentIdProfesional();
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellido").ascending());

        Page<Paciente> pacientes;
        if (q != null && !q.isBlank()) {
            pacientes = pacienteRepository.search(idSistema, q, pageable);
        } else {
            pacientes = pacienteRepository.findByProfesional_IdProfesionalAndSistema_IdSistemaAndBaja(
                    idProfesional, idSistema, false, pageable);
        }
        return pacientes.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PacienteResponse obtener(String id) {
        return toResponse(findById(id));
    }

    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        String idSistema     = SecurityContextUtil.getCurrentIdSistema();
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        // RN-P01: DNI unique per tenant
        if (pacienteRepository.existsByDniAndSistema_IdSistemaAndBaja(request.dni(), idSistema, false)) {
            throw new ConflictException(
                    "Ya existe un paciente activo con DNI " + request.dni() + " en este consultorio");
        }

        Sistema sistema = sistemaRepository.findById(idSistema)
                .orElseThrow(() -> new BadRequestException("Sistema no encontrado"));
        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new BadRequestException("Profesional no encontrado"));

        // idObraSocial now is a UUID String; null -> use default "Particular"
        ObraSocial obraSocial;
        if (request.idObraSocial() != null && !request.idObraSocial().isBlank()) {
            obraSocial = obraSocialRepository.findById(request.idObraSocial())
                    .orElseThrow(() -> new BadRequestException("Obra social no encontrada"));
        } else {
            obraSocial = obraSocialRepository.findByNombreIgnoreCase("Particular")
                    .orElseGet(() -> obraSocialRepository.save(ObraSocial.builder().nombre("Particular").baja(false).build()));
        }

        Paciente paciente = Paciente.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .dni(request.dni())
                .email(request.email())
                .celular(request.celular())
                .obraSocial(obraSocial)
                .nroAfiliado(request.nroAfiliado())
                .observaciones(request.observaciones())
                .profesional(profesional)
                .sistema(sistema)
                .baja(false)
                .build();
        // idPaciente generated in @PrePersist

        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse actualizar(String id, PacienteRequest request) {
        Paciente paciente = findById(id);
        String idSistema = SecurityContextUtil.getCurrentIdSistema();

        // RN-P01: if DNI changed, check uniqueness
        if (!paciente.getDni().equals(request.dni())) {
            if (pacienteRepository.existsByDniAndSistema_IdSistemaAndBaja(request.dni(), idSistema, false)) {
                throw new ConflictException("Ya existe un paciente activo con DNI " + request.dni());
            }
        }

        ObraSocial obraSocial;
        if (request.idObraSocial() != null && !request.idObraSocial().isBlank()) {
            obraSocial = obraSocialRepository.findById(request.idObraSocial())
                    .orElseThrow(() -> new BadRequestException("Obra social no encontrada"));
        } else {
            obraSocial = obraSocialRepository.findByNombreIgnoreCase("Particular")
                    .orElseGet(() -> obraSocialRepository.save(ObraSocial.builder().nombre("Particular").baja(false).build()));
        }

        paciente.setNombre(request.nombre());
        paciente.setApellido(request.apellido());
        paciente.setDni(request.dni());
        paciente.setEmail(request.email());
        paciente.setCelular(request.celular());
        paciente.setObraSocial(obraSocial);
        paciente.setNroAfiliado(request.nroAfiliado());
        paciente.setObservaciones(request.observaciones());

        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public void darDeBaja(String id, String idMotivo) {
        Paciente paciente = findById(id);

        // RN-P03: motivo obligatorio
        if (idMotivo == null || idMotivo.isBlank()) {
            throw new BadRequestException("El motivo de baja es obligatorio");
        }

        // RN-P02: verify no future confirmed turnos
        List<Turno> futurosTurnos = turnoRepository.findFutureConfirmedByPaciente(paciente.getIdPaciente());
        if (!futurosTurnos.isEmpty()) {
            throw new ConflictException("El paciente tiene " + futurosTurnos.size() +
                    " turno(s) futuro(s) confirmado(s). DebÃ©s cancelarlos antes de dar de baja.");
        }

        Motivo motivo = motivoRepository.findById(idMotivo)
                .orElseThrow(() -> new BadRequestException("Motivo de baja no vÃ¡lido"));

        paciente.setBaja(true);
        paciente.setMotivo(motivo);
        paciente.setFechaBaja(LocalDateTime.now());
        pacienteRepository.save(paciente);
    }

    private Paciente findById(String id) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        return pacienteRepository.findByIdPacienteAndSistema_IdSistemaAndBaja(id, idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
    }

    private PacienteResponse toResponse(Paciente p) {
        return new PacienteResponse(
                p.getIdPaciente(),   // UUID is now the PK
                p.getNombre(),
                p.getApellido(),
                p.getDni(),
                p.getEmail(),
                p.getCelular(),
                p.getObraSocial() != null ? p.getObraSocial().getNombre() : "Particular",
                p.getObraSocial() != null ? p.getObraSocial().getIdObraSocial() : null,
                p.getNroAfiliado(),
                p.getObservaciones(),
                p.getFechaCreacion());
    }
}

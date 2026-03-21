package com.psygst.service;

import com.psygst.exception.*;
import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.historiaclinica.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final ProfesionalRepository profesionalRepository;
    private final SistemaRepository sistemaRepository;
    private final EncryptionService encryptionService;
    private final AuditService auditService;

    /** RN-H04: all access is logged */
    @Transactional(readOnly = true)
    public List<HistoriaClinicaListResponse> listar(String pacienteUuid, String ipOrigen) {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        // RN-H01: only owner can read
        List<HistoriaClinica> notas = historiaClinicaRepository
                .findByPaciente_UuidAndProfesional_IdProfesionalAndBajaOrderByFechaCreacionDesc(
                        pacienteUuid, idProfesional, (byte) 0);

        auditService.log("VER_LISTA_HC", "HISTORIA_CLINICA",
                "{\"pacienteUuid\":\"" + pacienteUuid + "\"}", ipOrigen);

        // Return only metadata (resumen) - content NOT exposed in list (WF-07)
        return notas.stream().map(n -> new HistoriaClinicaListResponse(
                n.getUuid(), n.getResumen(), n.getFechaCreacion(),
                n.getTurno() != null ? n.getTurno().getUuid() : null)).collect(Collectors.toList());
    }

    /** RN-H01: owner only. RN-H02: decrypt. RN-H04: log */
    @Transactional(readOnly = true)
    public HistoriaClinicaResponse obtener(String uuid, String ipOrigen) {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        HistoriaClinica nota = historiaClinicaRepository
                .findByUuidAndProfesional_IdProfesionalAndBaja(uuid, idProfesional, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Nota clínica no encontrada"));

        auditService.log("VER_NOTA_HC", "HISTORIA_CLINICA",
                "{\"notaUuid\":\"" + uuid + "\"}", ipOrigen);

        String contenidoDescifrado = encryptionService.decrypt(nota.getContenido());

        return new HistoriaClinicaResponse(
                nota.getUuid(), contenidoDescifrado, nota.getResumen(),
                nota.getFechaCreacion(), nota.getFechaModificacion(),
                nota.getTurno() != null ? nota.getTurno().getUuid() : null);
    }

    /** RN-H02: encrypt content before saving. RN-H04: log */
    @Transactional
    public HistoriaClinicaListResponse crear(HistoriaClinicaRequest request, String ipOrigen) {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();

        Paciente paciente = pacienteRepository
                .findByUuidAndSistema_IdSistemaAndBaja(request.pacienteUuid(), idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));

        Sistema sistema = sistemaRepository.findById(idSistema)
                .orElseThrow(() -> new BadRequestException("Sistema no encontrado"));

        Turno turno = null;
        if (request.turnoUuid() != null) {
            turno = turnoRepository.findByUuidAndSistema_IdSistemaAndBaja(request.turnoUuid(), idSistema, (byte) 0)
                    .orElse(null);
        }

        // RN-H02: encrypt before storing
        String contenidoCifrado = encryptionService.encrypt(request.contenido());

        HistoriaClinica nota = HistoriaClinica.builder()
                .uuid(UUID.randomUUID().toString())
                .paciente(paciente)
                .turno(turno)
                .contenido(contenidoCifrado)
                .resumen(request.resumen())
                .profesional(profesional)
                .sistema(sistema)
                .baja((byte) 0)
                .build();

        nota = historiaClinicaRepository.save(nota);

        auditService.log("CREAR_NOTA_HC", "HISTORIA_CLINICA",
                "{\"notaUuid\":\"" + nota.getUuid() + "\",\"pacienteUuid\":\"" + request.pacienteUuid() + "\"}",
                ipOrigen);

        return new HistoriaClinicaListResponse(nota.getUuid(), nota.getResumen(), nota.getFechaCreacion(),
                turno != null ? turno.getUuid() : null);
    }

    /** RN-H03: no physical delete */
    @Transactional
    public void eliminar(String uuid) {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        HistoriaClinica nota = historiaClinicaRepository
                .findByUuidAndProfesional_IdProfesionalAndBaja(uuid, idProfesional, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Nota clínica no encontrada"));
        nota.setBaja((byte) 1); // logical delete only
        historiaClinicaRepository.save(nota);
    }
}

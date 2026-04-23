package com.psygst.service;

import com.psygst.dto.factura.FacturaResponse;
import com.psygst.exception.BadRequestException;
import com.psygst.model.Factura;
import com.psygst.model.Paciente;
import com.psygst.model.Profesional;
import com.psygst.repository.FacturaRepository;
import com.psygst.repository.PacienteRepository;
import com.psygst.repository.ProfesionalRepository;
import com.psygst.security.SecurityContextUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;

    @Transactional
    public FacturaResponse subirFactura(String idPaciente, MultipartFile file) {
        String idSistema     = SecurityContextUtil.getCurrentIdSistema();
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        if (file.isEmpty()) {
            throw new BadRequestException("El archivo estÃ¡ vacÃ­o");
        }

        Paciente paciente = pacienteRepository.findByIdPacienteAndSistema_IdSistemaAndBaja(idPaciente, idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));

        try {
            String originalName = file.getOriginalFilename();
            String fileNameToSave = originalName != null ? originalName : "factura.pdf";

            Factura factura = Factura.builder()
                    .paciente(paciente)
                    .profesional(profesional)
                    .sistema(paciente.getSistema())
                    .nombreArchivo(fileNameToSave)
                    .datosArchivo(file.getBytes())
                    .baja(false)
                    .build();
            // idFactura generated in @PrePersist

            factura = facturaRepository.save(factura);

            return new FacturaResponse(factura.getIdFactura(), factura.getNombreArchivo(),
                    factura.getFechaCreacion().toString());
        } catch (IOException e) {
            log.error("Error al guardar archivo", e);
            throw new RuntimeException("No se pudo guardar la factura", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarPorPaciente(String idPaciente) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        return facturaRepository.findByPaciente_IdPacienteAndSistema_IdSistemaAndBaja(idPaciente, idSistema, false)
                .stream()
                .map(f -> new FacturaResponse(f.getIdFactura(), f.getNombreArchivo(),
                        f.getFechaCreacion().toString()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource descargarFactura(String idFactura) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        Factura factura = facturaRepository.findByIdFacturaAndSistema_IdSistemaAndBaja(idFactura, idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (factura.getDatosArchivo() != null) {
            return new org.springframework.core.io.ByteArrayResource(factura.getDatosArchivo());
        } else {
            throw new EntityNotFoundException("La factura no contiene datos adjuntos");
        }
    }

    @Transactional(readOnly = true)
    public Factura obtenerPorId(String idFactura) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        return facturaRepository.findByIdFacturaAndSistema_IdSistemaAndBaja(idFactura, idSistema, false)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }
}

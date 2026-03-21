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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;

    // Almacenamiento migrado a DB

    @Transactional
    public FacturaResponse subirFactura(String pacienteUuid, MultipartFile file) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();

        if (file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        Paciente paciente = pacienteRepository.findByUuidAndSistema_IdSistemaAndBaja(pacienteUuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));

        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".pdf";
            String newFileName = UUID.randomUUID() + extension;
            String fileNameToSave = originalName != null ? originalName : newFileName;

            Factura factura = Factura.builder()
                    .uuid(UUID.randomUUID().toString())
                    .paciente(paciente)
                    .profesional(profesional)
                    .sistema(paciente.getSistema())
                    .nombreArchivo(fileNameToSave)
                    .datosArchivo(file.getBytes())
                    .baja((byte) 0)
                    .build();

            factura = facturaRepository.save(factura);

            return new FacturaResponse(factura.getUuid(), factura.getNombreArchivo(), factura.getFechaCreacion().toString());
        } catch (IOException e) {
            log.error("Error al guardar archivo", e);
            throw new RuntimeException("No se pudo guardar la factura", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarPorPaciente(String pacienteUuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        return facturaRepository.findByPaciente_UuidAndSistema_IdSistemaAndBaja(pacienteUuid, idSistema, (byte) 0)
                .stream()
                .map(f -> new FacturaResponse(f.getUuid(), f.getNombreArchivo(), f.getFechaCreacion().toString()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource descargarFactura(String uuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Factura factura = facturaRepository.findByUuidAndSistema_IdSistemaAndBaja(uuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));

        if (factura.getDatosArchivo() != null) {
            return new org.springframework.core.io.ByteArrayResource(factura.getDatosArchivo());
        } else {
            throw new EntityNotFoundException("La factura no contiene datos adjuntos");
        }
    }
    
    @Transactional(readOnly = true)
    public Factura obtenerPorUuid(String uuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        return facturaRepository.findByUuidAndSistema_IdSistemaAndBaja(uuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }
}

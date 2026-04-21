package com.psygst.service;

import com.psygst.dto.profesion.ProfesionResponse;
import com.psygst.dto.profesional.ProfesionalResponse;
import com.psygst.dto.profesional.ProfesionalUpdateRequest;
import com.psygst.exception.BadRequestException;
import com.psygst.model.Profesion;
import com.psygst.model.Profesional;
import com.psygst.repository.ProfesionRepository;
import com.psygst.repository.ProfesionalRepository;
import com.psygst.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final ProfesionRepository profesionRepository;

    @Transactional(readOnly = true)
    public ProfesionalResponse obtenerMe() {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        
        if (idProfesional == null) {
            throw new BadRequestException("El usuario actual no tiene un perfil de profesional asociado.");
        }
        
        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new BadRequestException("Profesional no encontrado"));
                
        return mapToResponse(profesional);
    }

    @Transactional
    public ProfesionalResponse actualizarMe(ProfesionalUpdateRequest request) {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        
        if (idProfesional == null) {
            throw new BadRequestException("El usuario actual no tiene un perfil de profesional asociado.");
        }
        
        Profesional profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new BadRequestException("Profesional no encontrado"));

        profesional.setNombre(request.nombre());
        profesional.setApellido(request.apellido());
        profesional.setCuit(request.cuit());
        profesional.setNroLicencia(request.nroLicencia());
        profesional.setEmail(request.email());
        profesional.setCelular(request.celular());
        profesional.setCbu(request.cbu());
        profesional.setAlias(request.alias());

        if (request.idProfesion() != null && !request.idProfesion().isBlank()) {
            Profesion profesion = profesionRepository.findById(request.idProfesion())
                    .orElseThrow(() -> new BadRequestException("Profesión no encontrada"));
            profesional.setProfesion(profesion);
        } else {
            profesional.setProfesion(null);
        }

        profesional = profesionalRepository.save(profesional);
        return mapToResponse(profesional);
    }
    
    private ProfesionalResponse mapToResponse(Profesional profesional) {
        ProfesionResponse profesionResponse = null;
        if (profesional.getProfesion() != null) {
            profesionResponse = new ProfesionResponse(
                    profesional.getProfesion().getIdProfesion(),
                    profesional.getProfesion().getNombre());
        }
        
        return new ProfesionalResponse(
                profesional.getIdProfesional(),
                profesional.getNombre(),
                profesional.getApellido(),
                profesional.getCuit(),
                profesional.getNroLicencia(),
                profesional.getEmail(),
                profesional.getCelular(),
                profesional.getCbu(),
                profesional.getAlias(),
                profesionResponse
        );
    }
}

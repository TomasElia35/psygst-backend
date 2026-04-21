package com.psygst.service;

import com.psygst.dto.profesion.ProfesionResponse;
import com.psygst.repository.ProfesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfesionService {

    private final ProfesionRepository profesionRepository;

    public List<ProfesionResponse> obtenerTodas() {
        return profesionRepository.findByBaja((byte) 0).stream()
                .map(p -> new ProfesionResponse(p.getIdProfesion(), p.getNombre()))
                .collect(Collectors.toList());
    }
}

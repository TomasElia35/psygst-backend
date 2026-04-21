package com.psygst.controller;

import com.psygst.dto.profesion.ProfesionResponse;
import com.psygst.service.ProfesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/profesiones")
@RequiredArgsConstructor
public class ProfesionController {

    private final ProfesionService profesionService;

    @GetMapping
    public ResponseEntity<List<ProfesionResponse>> obtenerTodas() {
        return ResponseEntity.ok(profesionService.obtenerTodas());
    }
}

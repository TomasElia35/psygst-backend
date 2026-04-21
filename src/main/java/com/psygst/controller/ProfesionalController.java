package com.psygst.controller;

import com.psygst.dto.profesional.ProfesionalResponse;
import com.psygst.dto.profesional.ProfesionalUpdateRequest;
import com.psygst.service.ProfesionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profesionales")
@RequiredArgsConstructor
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    @GetMapping("/me")
    public ResponseEntity<ProfesionalResponse> obtenerMe() {
        return ResponseEntity.ok(profesionalService.obtenerMe());
    }

    @PutMapping("/me")
    public ResponseEntity<ProfesionalResponse> actualizarMe(@Valid @RequestBody ProfesionalUpdateRequest request) {
        return ResponseEntity.ok(profesionalService.actualizarMe(request));
    }
}

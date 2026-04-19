package com.psygst.controller;

import com.psygst.dto.paciente.*;
import com.psygst.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    public ResponseEntity<Page<PacienteResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(pacienteService.listar(page, size, q));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PacienteResponse> obtener(@PathVariable String uuid) {
        return ResponseEntity.ok(pacienteService.obtener(uuid));
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(request));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PacienteResponse> actualizar(
            @PathVariable String uuid, @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(pacienteService.actualizar(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> darDeBaja(
            @PathVariable String uuid,
            @RequestParam String idMotivo) {
        pacienteService.darDeBaja(uuid, idMotivo);
        return ResponseEntity.noContent().build();
    }
}

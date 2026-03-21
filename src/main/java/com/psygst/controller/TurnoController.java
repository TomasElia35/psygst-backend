package com.psygst.controller;

import com.psygst.dto.turno.*;
import com.psygst.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping("/semana")
    public ResponseEntity<List<TurnoResponse>> obtenerSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio) {
        return ResponseEntity.ok(turnoService.obtenerSemana(fechaInicio));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<TurnoResponse> obtener(@PathVariable String uuid) {
        return ResponseEntity.ok(turnoService.obtener(uuid));
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(request));
    }

    @PatchMapping("/{uuid}/estado")
    public ResponseEntity<TurnoResponse> cambiarEstado(
            @PathVariable String uuid, @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(turnoService.cambiarEstado(uuid, request.estado()));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<TurnoResponse> actualizar(
            @PathVariable String uuid, @Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.ok(turnoService.actualizar(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> eliminar(@PathVariable String uuid) {
        turnoService.eliminar(uuid);
        return ResponseEntity.noContent().build();
    }
}

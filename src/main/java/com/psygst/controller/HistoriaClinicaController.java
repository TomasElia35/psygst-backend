package com.psygst.controller;

import com.psygst.dto.historiaclinica.*;
import com.psygst.service.HistoriaClinicaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historia-clinica")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaService servicioHC;

    @GetMapping("/paciente/{pacienteUuid}")
    public ResponseEntity<List<HistoriaClinicaListResponse>> listar(
            @PathVariable String pacienteUuid, HttpServletRequest req) {
        return ResponseEntity.ok(servicioHC.listar(pacienteUuid, req.getRemoteAddr()));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<HistoriaClinicaResponse> obtener(
            @PathVariable String uuid, HttpServletRequest req) {
        return ResponseEntity.ok(servicioHC.obtener(uuid, req.getRemoteAddr()));
    }

    @PostMapping
    public ResponseEntity<HistoriaClinicaListResponse> crear(
            @Valid @RequestBody HistoriaClinicaRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioHC.crear(request, req.getRemoteAddr()));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> eliminar(@PathVariable String uuid) {
        servicioHC.eliminar(uuid);
        return ResponseEntity.noContent().build();
    }
}

package com.psygst.controller;

import com.psygst.model.Motivo;
import com.psygst.repository.MotivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/motivos")
@RequiredArgsConstructor
public class MotivoController {

    private final MotivoRepository motivoRepository;

    @GetMapping
    public ResponseEntity<List<Motivo>> listar() {
        return ResponseEntity.ok(motivoRepository.findByBaja(false));
    }
}

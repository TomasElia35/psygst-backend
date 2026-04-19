package com.psygst.controller;

import com.psygst.model.ObraSocial;
import com.psygst.repository.ObraSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/obras-sociales")
@RequiredArgsConstructor
public class ObraSocialController {

    private final ObraSocialRepository obraSocialRepository;

    @GetMapping
    public ResponseEntity<List<ObraSocial>> listar() {
        return ResponseEntity.ok(obraSocialRepository.findByBaja((byte) 0));
    }
}

package com.psygst.controller;

import com.psygst.model.Rol;
import com.psygst.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository rolRepository;

    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        // Only expose active roles (baja = 0)
        return ResponseEntity.ok(
                rolRepository.findAll().stream()
                        .filter(r -> r.getBaja() == false)
                        .toList());
    }
}

package com.psygst.controller;

import com.psygst.dto.auth.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@RequestBody CambiarPasswordRequest request) {
        String username = SecurityContextUtil.getCurrentUser().getUsername();
        authService.cambiarPassword(username, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT is stateless — client should discard the token
        return ResponseEntity.noContent().build();
    }

}

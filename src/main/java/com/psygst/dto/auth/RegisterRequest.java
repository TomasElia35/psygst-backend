package com.psygst.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull String idRol,     // UUID of the Rol entity (was Integer)
        @NotBlank String nombre,
        @NotBlank String apellido,
        String email,
        String celular
) {}

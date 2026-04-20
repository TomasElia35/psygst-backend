package com.psygst.dto.paciente;

import jakarta.validation.constraints.*;

public record PacienteRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotBlank(message = "El DNI es obligatorio") String dni,
        @Email(message = "Formato de email inválido") String email,
        @Pattern(regexp = "^$|^[0-9]{8,15}$", message = "El celular debe tener entre 8 y 15 dígitos numéricos o estar vacío") String celular,
        String idObraSocial,    // UUID of ObraSocial (was Integer)
        String nroAfiliado,
        String observaciones) {
}

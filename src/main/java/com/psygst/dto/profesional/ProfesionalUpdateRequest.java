package com.psygst.dto.profesional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfesionalUpdateRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String apellido,

    @Size(max = 20)
    String cuit,

    @Size(max = 50)
    String nroLicencia,

    @Size(max = 100)
    String email,

    @Size(max = 20)
    String celular,

    @Size(max = 100)
    String cbu,

    @Size(max = 50)
    String alias,

    String idProfesion
) {}

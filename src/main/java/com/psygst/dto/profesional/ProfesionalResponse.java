package com.psygst.dto.profesional;

import com.psygst.dto.profesion.ProfesionResponse;

public record ProfesionalResponse(
    String idProfesional,
    String nombre,
    String apellido,
    String cuit,
    String nroLicencia,
    String email,
    String celular,
    String cbu,
    String alias,
    ProfesionResponse profesion
) {}

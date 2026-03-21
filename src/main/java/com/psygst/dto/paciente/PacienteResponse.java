package com.psygst.dto.paciente;

import java.time.LocalDateTime;

public record PacienteResponse(
        String uuid,
        String nombre,
        String apellido,
        String dni,
        String email,
        String celular,
        String obraSocialNombre,
        Integer idObraSocial,
        String nroAfiliado,
        String observaciones,
        LocalDateTime fechaCreacion) {
}

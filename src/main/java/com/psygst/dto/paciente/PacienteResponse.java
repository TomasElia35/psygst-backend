package com.psygst.dto.paciente;

import java.time.LocalDateTime;

public record PacienteResponse(
        String uuid,            // = idPaciente (the UUID PK)
        String nombre,
        String apellido,
        String dni,
        String email,
        String celular,
        String obraSocialNombre,
        String idObraSocial,    // was Integer, now String (UUID)
        String nroAfiliado,
        String observaciones,
        LocalDateTime fechaCreacion) {
}

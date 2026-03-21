package com.psygst.dto.turno;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoRequest(
        @NotBlank(message = "El paciente es obligatorio") String pacienteUuid,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaComienzo,
        @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
        @NotBlank(message = "La modalidad es obligatoria") String modalidad,
        @NotNull(message = "El precio es obligatorio") @DecimalMin("0.01") BigDecimal precioFinal,
        String observaciones) {
}

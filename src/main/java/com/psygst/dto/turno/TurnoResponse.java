package com.psygst.dto.turno;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoResponse(
        String uuid,
        String pacienteUuid,
        String pacienteNombreCompleto,
        LocalDate fecha,
        LocalTime horaComienzo,
        LocalTime horaFin,
        String modalidad,
        String estado,
        BigDecimal precioFinal,
        String observaciones) {
}

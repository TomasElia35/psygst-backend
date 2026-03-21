package com.psygst.dto.historiaclinica;

import jakarta.validation.constraints.NotBlank;

public record HistoriaClinicaRequest(
        @NotBlank String pacienteUuid,
        @NotBlank String contenido,
        String resumen,
        String turnoUuid) {
}

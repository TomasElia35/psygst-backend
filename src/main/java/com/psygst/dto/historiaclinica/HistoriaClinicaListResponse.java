package com.psygst.dto.historiaclinica;

import java.time.LocalDateTime;

public record HistoriaClinicaListResponse(
        String uuid,
        String resumen,
        LocalDateTime fechaCreacion,
        String turnoUuid) {
}

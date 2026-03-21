package com.psygst.dto.historiaclinica;

import java.time.LocalDateTime;

public record HistoriaClinicaResponse(
        String uuid,
        String contenido, // decrypted on read
        String resumen,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion,
        String turnoUuid) {
}

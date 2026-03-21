package com.psygst.dto.notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        String uuid,
        String tipo,
        String canal,
        String estado,
        Integer intentos,
        String detalle,
        LocalDateTime fechaProgramada,
        LocalDateTime fechaEnvio,
        String turnoUuid,
        String pacienteNombreCompleto) {
}

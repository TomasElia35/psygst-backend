package com.psygst.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagoResponse(
        String uuid,
        String turnoUuid,
        String pacienteNombreCompleto,
        LocalDate fechaTurno,
        BigDecimal monto,
        Boolean pagado,
        String metodoPago,
        String comprobanteImg,
        LocalDateTime fechaPago) {
}

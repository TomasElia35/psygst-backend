package com.psygst.dto.recibo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReciboResponse(
        String uuid,
        String nroRecibo,
        BigDecimal montoTotal,
        LocalDateTime fechaEmision,
        String rutaPdf) {
}

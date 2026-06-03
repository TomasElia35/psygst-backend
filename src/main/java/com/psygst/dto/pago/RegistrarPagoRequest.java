package com.psygst.dto.pago;

import java.math.BigDecimal;

public record RegistrarPagoRequest(String metodoPago, String comprobanteImg, String moneda, BigDecimal cotizacion) {
}

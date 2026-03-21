package com.psygst.dto.factura;

public record FacturaResponse(
    String uuid,
    String nombreArchivo,
    String fechaCreacion
) {}

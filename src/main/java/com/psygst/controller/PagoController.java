package com.psygst.controller;

import com.psygst.dto.pago.*;
import com.psygst.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final ExcelService excelService;

    @GetMapping("/turno/{turnoUuid}")
    public ResponseEntity<PagoResponse> obtenerPorTurno(@PathVariable String turnoUuid) {
        return ResponseEntity.ok(pagoService.obtenerPorTurno(turnoUuid));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PagoResponse>> obtenerPendientes() {
        return ResponseEntity.ok(pagoService.obtenerPendientes());
    }

    @GetMapping("/pagados")
    public ResponseEntity<List<PagoResponse>> obtenerPagados() {
        return ResponseEntity.ok(pagoService.obtenerPagados());
    }

    @PatchMapping("/{uuid}/registrar")
    public ResponseEntity<PagoResponse> registrarPago(
            @PathVariable String uuid, @RequestBody RegistrarPagoRequest request) {
        return ResponseEntity.ok(pagoService.registrarPago(uuid, request));
    }

    @GetMapping("/reporte-mensual")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam int year, @RequestParam int month) {
        byte[] xlsx = excelService.generarReporteMensual(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte-" + year + "-" + String.format("%02d", month) + ".xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}

package com.psygst.controller;

import com.psygst.dto.recibo.ReciboResponse;
import com.psygst.service.ReciboService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recibos")
@RequiredArgsConstructor
public class ReciboController {

    private final ReciboService reciboService;

    @PostMapping("/generar/{pagoUuid}")
    public ResponseEntity<ReciboResponse> generar(@PathVariable String pagoUuid) {
        return ResponseEntity.ok(reciboService.generar(pagoUuid));
    }

    @GetMapping("/{uuid}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable String uuid) {
        byte[] pdf = reciboService.descargar(uuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo-" + uuid + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{uuid}/anular")
    public ResponseEntity<Void> anular(@PathVariable String uuid) {
        reciboService.anular(uuid);
        return ResponseEntity.noContent().build();
    }
}

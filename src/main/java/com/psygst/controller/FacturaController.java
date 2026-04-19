package com.psygst.controller;

import com.psygst.dto.factura.FacturaResponse;
import com.psygst.model.Factura;
import com.psygst.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping("/paciente/{pacienteUuid}")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaResponse subirFactura(@PathVariable String pacienteUuid, @RequestParam("file") MultipartFile file) {
        return facturaService.subirFactura(pacienteUuid, file);
    }

    @GetMapping("/paciente/{pacienteUuid}")
    public List<FacturaResponse> listarPorPaciente(@PathVariable String pacienteUuid) {
        return facturaService.listarPorPaciente(pacienteUuid);
    }

    @GetMapping("/{uuid}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable String uuid) {
        Factura factura = facturaService.obtenerPorId(uuid);
        Resource resource = facturaService.descargarFactura(uuid);

        String contentType = "application/octet-stream";
        if (factura.getNombreArchivo().toLowerCase().endsWith(".pdf")) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + factura.getNombreArchivo() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}

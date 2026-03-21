package com.psygst.controller;

import com.psygst.dto.notificacion.NotificacionResponse;
import com.psygst.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/turno/{turnoUuid}")
    public ResponseEntity<List<NotificacionResponse>> obtenerPorTurno(@PathVariable String turnoUuid) {
        return ResponseEntity.ok(notificacionService.obtenerPorTurno(turnoUuid));
    }

    @GetMapping("/fallidas")
    public ResponseEntity<List<NotificacionResponse>> obtenerFallidas() {
        return ResponseEntity.ok(notificacionService.obtenerFallidas());
    }

    @PostMapping("/{uuid}/reenviar")
    public ResponseEntity<Void> reenviar(@PathVariable String uuid) {
        notificacionService.reenviar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enviar-pago/{turnoUuid}")
    public ResponseEntity<Void> enviarDatosPago(@PathVariable String turnoUuid) {
        notificacionService.enviarDatosPago(turnoUuid);
        return ResponseEntity.noContent().build();
    }
}

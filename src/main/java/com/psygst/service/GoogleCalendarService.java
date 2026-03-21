package com.psygst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub para la integración con Google Calendar (V1.0).
 * En la versión V2.0 aquí se implementaría el flujo OAuth2.
 */
@Service
@Slf4j
public class GoogleCalendarService {

    public void sincronizarTurno(String turnoUuid) {
        log.info("[GOOGLE CALENDAR STUB] Sincronizando turno {} con Google Calendar...", turnoUuid);
        // TODO: Implementar en V2.0
    }

    public void eliminarTurno(String turnoUuid) {
        log.info("[GOOGLE CALENDAR STUB] Eliminando turno {} de Google Calendar...", turnoUuid);
        // TODO: Implementar en V2.0
    }
}

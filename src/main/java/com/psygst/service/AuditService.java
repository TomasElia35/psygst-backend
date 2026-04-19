package com.psygst.service;

import com.psygst.model.*;
import com.psygst.repository.LogGeneralRepository;
import com.psygst.repository.SistemaRepository;
import com.psygst.security.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final LogGeneralRepository logGeneralRepository;
    private final SistemaRepository sistemaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String accion, String modulo, String datos, String ipOrigen) {
        try {
            String idSistema = SecurityContextUtil.getCurrentIdSistema();
            String idUsuario = SecurityContextUtil.getCurrentIdAuth(); // String UUID

            Sistema sistema = sistemaRepository.findById(idSistema).orElse(null);

            LogGeneral entry = LogGeneral.builder()
                    .idUsuario(idUsuario)
                    .accion(accion)
                    .modulo(modulo)
                    .datos(datos)
                    .ipOrigen(ipOrigen)
                    .timestamp(LocalDateTime.now())
                    .sistema(sistema)
                    .baja((byte) 0)
                    .build();

            logGeneralRepository.save(entry);
        } catch (Exception e) {
            log.error("Error writing audit log: {}", e.getMessage());
        }
    }
}

package com.psygst.service;

import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.SecurityContextUtil;
import com.psygst.dto.pago.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;

    /** RN-F03: called automatically by TurnoService on turno creation */
    @Transactional
    public void crearPagoParaTurno(Turno turno) {
        Pago pago = Pago.builder()
                .uuid(UUID.randomUUID().toString())
                .turno(turno)
                .pagado(false)
                .monto(turno.getPrecioFinal())
                .profesional(turno.getProfesional())
                .sistema(turno.getSistema())
                .baja((byte) 0)
                .build();
        pagoRepository.save(pago);
    }

    /** RN-F04: called by TurnoService when canceled > 48hs before */
    @Transactional
    public void anularPagoDeTurnoPorUuid(String turnoUuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        pagoRepository.findAll().stream()
                .filter(p -> p.getTurno().getUuid().equals(turnoUuid)
                        && p.getSistema().getIdSistema().equals(idSistema)
                        && p.getBaja() == 0)
                .forEach(p -> {
                    p.setBaja((byte) 1);
                    pagoRepository.save(p);
                });
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorTurno(String turnoUuid) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        // Find turno indirectly via pago
        Pago pago = pagoRepository.findAll().stream()
                .filter(p -> p.getTurno().getUuid().equals(turnoUuid)
                        && p.getSistema().getIdSistema().equals(idSistema)
                        && p.getBaja() == 0)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para el turno"));
        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPendientes() {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        return pagoRepository.findPendientesByProfesional(idProfesional)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagados() {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        return pagoRepository.findPagadosByProfesional(idProfesional)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse registrarPago(String uuid, RegistrarPagoRequest request) {
        Integer idSistema = SecurityContextUtil.getCurrentIdSistema();
        Pago pago = pagoRepository.findByUuidAndSistema_IdSistemaAndBaja(uuid, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        pago.setPagado(true);
        pago.setMetodoPago(request.metodoPago());
        pago.setComprobanteImg(request.comprobanteImg());
        pago.setFechaPago(LocalDateTime.now());

        return toResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> getReporteMensual(int year, int month) {
        Integer idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        LocalDate inicio = LocalDate.of(year, month, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return pagoRepository.findByProfesionalAndPeriod(idProfesional, inicio, fin)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PagoResponse toResponse(Pago p) {
        return new PagoResponse(
                p.getUuid(),
                p.getTurno().getUuid(),
                p.getTurno().getPaciente().getNombre() + " " + p.getTurno().getPaciente().getApellido(),
                p.getTurno().getFecha(),
                p.getMonto(),
                p.getPagado(),
                p.getMetodoPago(),
                p.getComprobanteImg(),
                p.getFechaPago());
    }
}

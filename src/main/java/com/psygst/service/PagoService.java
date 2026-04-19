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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;

    /** RN-F03: called automatically by TurnoService on turno creation */
    @Transactional
    public void crearPagoParaTurno(Turno turno) {
        Pago pago = Pago.builder()
                .turno(turno)
                .pagado(false)
                .monto(turno.getPrecioFinal())
                .profesional(turno.getProfesional())
                .sistema(turno.getSistema())
                .baja((byte) 0)
                .build();
        // idPago generated in @PrePersist
        pagoRepository.save(pago);
    }

    /** RN-F04: called by TurnoService when canceled ≥ 48hs before */
    @Transactional
    public void anularPagoDeTurnoPorId(String idTurno) {
        pagoRepository.findByTurno_IdTurnoAndBaja(idTurno, (byte) 0)
                .ifPresent(p -> {
                    p.setBaja((byte) 1);
                    pagoRepository.save(p);
                });
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorTurno(String idTurno) {
        Pago pago = pagoRepository.findByTurno_IdTurnoAndBaja(idTurno, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para el turno"));
        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPendientes() {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        return pagoRepository.findPendientesByProfesional(idProfesional)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> obtenerPagados() {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        return pagoRepository.findPagadosByProfesional(idProfesional)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse registrarPago(String idPago, RegistrarPagoRequest request) {
        String idSistema = SecurityContextUtil.getCurrentIdSistema();
        Pago pago = pagoRepository.findByIdPagoAndSistema_IdSistemaAndBaja(idPago, idSistema, (byte) 0)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        pago.setPagado(true);
        pago.setMetodoPago(request.metodoPago());
        pago.setComprobanteImg(request.comprobanteImg());
        pago.setFechaPago(LocalDateTime.now());

        return toResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> getReporteMensual(int year, int month) {
        String idProfesional = SecurityContextUtil.getCurrentIdProfesional();
        LocalDate inicio = LocalDate.of(year, month, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return pagoRepository.findByProfesionalAndPeriod(idProfesional, inicio, fin)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PagoResponse toResponse(Pago p) {
        return new PagoResponse(
                p.getIdPago(),                      // UUID PK
                p.getTurno().getIdTurno(),           // UUID PK
                p.getTurno().getPaciente().getNombre() + " " + p.getTurno().getPaciente().getApellido(),
                p.getTurno().getFecha(),
                p.getMonto(),
                p.getPagado(),
                p.getMetodoPago(),
                p.getComprobanteImg(),
                p.getFechaPago());
    }
}

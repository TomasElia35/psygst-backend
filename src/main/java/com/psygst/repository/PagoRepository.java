package com.psygst.repository;

import com.psygst.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, String> {

        Optional<Pago> findByTurno_IdTurnoAndBaja(String idTurno, Boolean baja);

        Optional<Pago> findByIdPagoAndSistema_IdSistemaAndBaja(String idPago, String idSistema, Boolean baja);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = false")
        List<Pago> findPendientesByProfesional(@Param("idProfesional") String idProfesional);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = true")
        org.springframework.data.domain.Page<Pago> findPagadosWithoutSearch(
                        @Param("idProfesional") String idProfesional, 
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = true " +
                        "AND (LOWER(p.turno.paciente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
                        "OR LOWER(p.turno.paciente.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
        org.springframework.data.domain.Page<Pago> findPagadosWithSearch(
                        @Param("idProfesional") String idProfesional, 
                        @Param("busqueda") String busqueda, 
                        org.springframework.data.domain.Pageable pageable);

        // ─── Con filtro de mes/año ──────────────────────────────────────────────
        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = true " +
                        "AND p.fechaPago >= :inicio AND p.fechaPago < :fin")
        org.springframework.data.domain.Page<Pago> findPagadosByMesWithoutSearch(
                        @Param("idProfesional") String idProfesional,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = true " +
                        "AND p.fechaPago >= :inicio AND p.fechaPago < :fin " +
                        "AND (LOWER(p.turno.paciente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
                        "OR LOWER(p.turno.paciente.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
        org.springframework.data.domain.Page<Pago> findPagadosByMesWithSearch(
                        @Param("idProfesional") String idProfesional,
                        @Param("busqueda") String busqueda,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin,
                        org.springframework.data.domain.Pageable pageable);

        // ─── Pendientes filtrados por mes de turno ─────────────────────────────
        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.pagado = false " +
                        "AND p.turno.fecha >= :inicio AND p.turno.fecha <= :fin")
        List<Pago> findPendientesByMes(
                        @Param("idProfesional") String idProfesional,
                        @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = false AND p.turno.fecha >= :inicio AND p.turno.fecha <= :fin")
        List<Pago> findByProfesionalAndPeriod(@Param("idProfesional") String idProfesional,
                        @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);
}

package com.psygst.repository;

import com.psygst.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, String> {

    List<Turno> findByProfesional_IdProfesionalAndFechaAndBaja(
            String idProfesional, LocalDate fecha, Byte baja);

    @Query("SELECT t FROM Turno t WHERE t.profesional.idProfesional = :idProfesional " +
            "AND t.baja = 0 AND t.fecha >= :fechaInicio AND t.fecha <= :fechaFin")
    List<Turno> findByProfesionalAndWeek(@Param("idProfesional") String idProfesional,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * RN-T01: Overlap check — finds any ACTIVE (CONFIRMADO or REALIZADO) turno
     * for the same profesional on same date that overlaps the given time range.
     * A turno is CANCELADO or has baja=1 → does NOT block.
     * Overlap formula: existing.horaComienzo < newFin AND existing.horaFin > newComienzo
     */
    @Query("SELECT COUNT(t) > 0 FROM Turno t " +
            "WHERE t.profesional.idProfesional = :idProfesional " +
            "AND t.fecha = :fecha " +
            "AND t.baja = 0 " +
            "AND t.estado IN ('CONFIRMADO', 'REALIZADO') " +
            "AND t.horaComienzo < :horaFin " +
            "AND t.horaFin > :horaComienzo " +
            "AND (:excludeId IS NULL OR t.idTurno <> :excludeId)")
    boolean existsOverlap(@Param("idProfesional") String idProfesional,
            @Param("fecha") LocalDate fecha,
            @Param("horaComienzo") LocalTime horaComienzo,
            @Param("horaFin") LocalTime horaFin,
            @Param("excludeId") String excludeId);

    /** RN-P02: find future confirmed turnos for a patient */
    @Query("SELECT t FROM Turno t WHERE t.paciente.idPaciente = :idPaciente " +
            "AND t.estado = 'CONFIRMADO' AND t.fecha > CURRENT_DATE AND t.baja = 0")
    List<Turno> findFutureConfirmedByPaciente(@Param("idPaciente") String idPaciente);

    List<Turno> findByPaciente_IdPacienteAndBajaOrderByFechaDesc(String idPaciente, Byte baja);

    @Query("SELECT t FROM Turno t WHERE t.idTurno = :idTurno AND t.sistema.idSistema = :idSistema AND t.baja = :baja")
    java.util.Optional<Turno> findByIdTurnoAndSistema_IdSistemaAndBaja(
            @Param("idTurno") String idTurno, @Param("idSistema") String idSistema, @Param("baja") Byte baja);
}

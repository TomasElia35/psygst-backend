package com.psygst.repository;

import com.psygst.model.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReciboRepository extends JpaRepository<Recibo, Integer> {

    Optional<Recibo> findByUuidAndSistema_IdSistemaAndBaja(String uuid, Integer idSistema, Byte baja);

    /** RN-F02: max correlative number for prof in a given year */
    @Query("SELECT MAX(r.nroRecibo) FROM Recibo r " +
            "WHERE r.profesional.idProfesional = :idProfesional " +
            "AND YEAR(r.fechaEmision) = :year AND r.baja = 0")
    Optional<String> findLastNroReciboByProfesionalAndYear(
            @Param("idProfesional") Integer idProfesional, @Param("year") int year);

    List<Recibo> findByPago_Turno_Paciente_IdPacienteAndBajaOrderByFechaEmisionDesc(
            Integer idPaciente, Byte baja);
}

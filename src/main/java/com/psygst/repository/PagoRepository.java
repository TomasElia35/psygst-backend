package com.psygst.repository;

import com.psygst.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

        Optional<Pago> findByTurno_IdTurnoAndBaja(Integer idTurno, Byte baja);

        Optional<Pago> findByUuidAndSistema_IdSistemaAndBaja(String uuid, Integer idSistema, Byte baja);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = 0 AND p.pagado = false")
        List<Pago> findPendientesByProfesional(@Param("idProfesional") Integer idProfesional);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = 0 AND p.pagado = true ORDER BY p.fechaPago DESC")
        List<Pago> findPagadosByProfesional(@Param("idProfesional") Integer idProfesional);

        @Query("SELECT p FROM Pago p WHERE p.profesional.idProfesional = :idProfesional " +
                        "AND p.baja = 0 AND p.turno.fecha >= :inicio AND p.turno.fecha <= :fin")
        List<Pago> findByProfesionalAndPeriod(@Param("idProfesional") Integer idProfesional,
                        @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);
}

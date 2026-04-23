package com.psygst.repository;

import com.psygst.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, String> {

    Optional<Notificacion> findByIdNotificacionAndSistema_IdSistemaAndBaja(String idNotificacion, String idSistema, Boolean baja);

    List<Notificacion> findByTurno_IdTurnoAndBaja(String idTurno, Boolean baja);

    /**
     * Scheduler query: due notifications that are still pending and within retry limit
     */
    @Query("SELECT n FROM Notificacion n WHERE n.estado = 'PENDIENTE' " +
            "AND n.baja = 0 AND n.intentos < 3 " +
            "AND n.fechaProgramada <= :now")
    List<Notificacion> findDueNotificaciones(@Param("now") LocalDateTime now);

    List<Notificacion> findByProfesional_IdProfesionalAndEstadoAndBaja(
            String idProfesional, String estado, Boolean baja);

    List<Notificacion> findByProfesional_IdProfesionalAndBajaOrderByFechaCreacionDesc(
            String idProfesional, Boolean baja);

    /** Anular pending reminders when turno is cancelled (RN-N03) */
    @Query("SELECT n FROM Notificacion n WHERE n.turno.idTurno = :idTurno " +
            "AND n.tipo = 'RECORDATORIO_24HS' AND n.estado = 'PENDIENTE' AND n.baja = 0")
    List<Notificacion> findPendingRemindersByTurno(@Param("idTurno") String idTurno);
}

package com.psygst.repository;

import com.psygst.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, String> {

    /** RN-P01: DNI unique per tenant among active patients */
    boolean existsByDniAndSistema_IdSistemaAndBaja(String dni, String idSistema, Byte baja);

    Optional<Paciente> findByIdPacienteAndSistema_IdSistemaAndBaja(String idPaciente, String idSistema, Byte baja);

    Page<Paciente> findByProfesional_IdProfesionalAndSistema_IdSistemaAndBaja(
            String idProfesional, String idSistema, Byte baja, Pageable pageable);

    @Query("SELECT p FROM Paciente p WHERE p.sistema.idSistema = :idSistema AND p.baja = 0 " +
            "AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%')) OR p.dni LIKE CONCAT('%', :q, '%'))")
    Page<Paciente> search(@Param("idSistema") String idSistema, @Param("q") String query, Pageable pageable);
}

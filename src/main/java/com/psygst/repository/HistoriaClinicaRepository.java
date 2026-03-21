package com.psygst.repository;

import com.psygst.model.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {

    /** RN-H01: owner check — always filter by idProfesional */
    List<HistoriaClinica> findByPaciente_UuidAndProfesional_IdProfesionalAndBajaOrderByFechaCreacionDesc(
            String pacienteUuid, Integer idProfesional, Byte baja);

    Optional<HistoriaClinica> findByUuidAndProfesional_IdProfesionalAndBaja(
            String uuid, Integer idProfesional, Byte baja);
}

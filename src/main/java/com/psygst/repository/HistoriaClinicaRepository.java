package com.psygst.repository;

import com.psygst.model.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, String> {

    /** RN-H01: owner check — always filter by idProfesional */
    List<HistoriaClinica> findByPaciente_IdPacienteAndProfesional_IdProfesionalAndBajaOrderByFechaCreacionDesc(
            String idPaciente, String idProfesional, Byte baja);

    Optional<HistoriaClinica> findByIdHistoriaClinicaAndProfesional_IdProfesionalAndBaja(
            String idHistoriaClinica, String idProfesional, Byte baja);
}

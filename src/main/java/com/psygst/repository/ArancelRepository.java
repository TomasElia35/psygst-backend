package com.psygst.repository;

import com.psygst.model.Arancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArancelRepository extends JpaRepository<Arancel, Integer> {

    List<Arancel> findByProfesional_IdProfesionalAndBaja(Integer idProfesional, Byte baja);

    Optional<Arancel> findByProfesional_IdProfesionalAndObraSocial_IdObraSocialAndModalidadAndBaja(
            Integer idProfesional, Integer idObraSocial, String modalidad, Byte baja);
}

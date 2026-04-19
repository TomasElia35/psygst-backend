package com.psygst.repository;

import com.psygst.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, String> {
    Optional<Profesional> findByIdProfesionalAndBaja(String idProfesional, Byte baja);
}

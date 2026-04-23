package com.psygst.repository;

import com.psygst.model.Profesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesionRepository extends JpaRepository<Profesion, String> {
    List<Profesion> findByBaja(Boolean baja);
}

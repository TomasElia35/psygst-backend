package com.psygst.repository;

import com.psygst.model.ObraSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraSocialRepository extends JpaRepository<ObraSocial, String> {
    List<ObraSocial> findByBaja(Boolean baja);
    java.util.Optional<ObraSocial> findByNombreIgnoreCase(String nombre);
}

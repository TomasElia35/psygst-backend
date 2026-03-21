package com.psygst.repository;

import com.psygst.model.Motivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotivoRepository extends JpaRepository<Motivo, Integer> {
    List<Motivo> findByBaja(Byte baja);
}

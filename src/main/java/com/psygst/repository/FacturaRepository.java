package com.psygst.repository;

import com.psygst.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    List<Factura> findByPaciente_UuidAndSistema_IdSistemaAndBaja(String pacienteUuid, Integer idSistema, Byte baja);
    Optional<Factura> findByUuidAndSistema_IdSistemaAndBaja(String uuid, Integer idSistema, Byte baja);
}

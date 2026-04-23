package com.psygst.repository;

import com.psygst.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {
    List<Factura> findByPaciente_IdPacienteAndSistema_IdSistemaAndBaja(String idPaciente, String idSistema, Boolean baja);
    Optional<Factura> findByIdFacturaAndSistema_IdSistemaAndBaja(String idFactura, String idSistema, Boolean baja);
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Facturas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factura {

    @Id
    @Column(name = "IdFactura", length = 36)
    private String idFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPaciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @Lob
    private byte[] datosArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema", nullable = false)
    private Sistema sistema;

    @Builder.Default
    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (idFactura == null) idFactura = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

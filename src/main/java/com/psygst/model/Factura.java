package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Facturas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factura {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFactura;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @Lob
    private byte[] datosArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProfesional", nullable = false)
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema", nullable = false)
    private Sistema sistema;

    @Builder.Default
    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { fechaCreacion = LocalDateTime.now(); }
}

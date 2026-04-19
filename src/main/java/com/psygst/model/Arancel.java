package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Arancel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Arancel {

    @Id
    @Column(name = "IdArancel", length = 36)
    private String idArancel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObraSocial")
    private ObraSocial obraSocial;

    @Column(nullable = false, length = 20)
    private String modalidad;  // PRESENCIAL, VIRTUAL

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (idArancel == null) idArancel = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { fechaModificacion = LocalDateTime.now(); }
}

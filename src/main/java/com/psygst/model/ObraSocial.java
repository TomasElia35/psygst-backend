package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "T_ObraSocial")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ObraSocial {

    @Id
    @Column(name = "IdObraSocial", columnDefinition = "uuid")
    private String idObraSocial;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String contactoLiquidacion;

    @Column(nullable = false)
    private Boolean baja = false;

    @PrePersist
    protected void onCreate() {
        if (idObraSocial == null) idObraSocial = UUID.randomUUID().toString();
    }
}

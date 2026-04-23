package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "T_Motivo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Motivo {

    @Id
    @Column(name = "IdMotivo", columnDefinition = "uuid")
    private String idMotivo;

    @Column(nullable = false, length = 100)
    private String descripcion;  // Alta, Abandono, DerivaciÃ³n, etc.

    @Column(nullable = false)
    private Boolean baja = false;

    @PrePersist
    protected void onCreate() {
        if (idMotivo == null) idMotivo = UUID.randomUUID().toString();
    }
}

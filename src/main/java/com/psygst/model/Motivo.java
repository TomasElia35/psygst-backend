package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "T_Motivo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Motivo {

    @Id
    @Column(name = "IdMotivo", length = 36)
    private String idMotivo;

    @Column(nullable = false, length = 100)
    private String descripcion;  // Alta, Abandono, Derivación, etc.

    @Column(nullable = false)
    private Byte baja = 0;

    @PrePersist
    protected void onCreate() {
        if (idMotivo == null) idMotivo = UUID.randomUUID().toString();
    }
}

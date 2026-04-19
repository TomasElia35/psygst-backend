package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "T_Rol")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {

    @Id
    @Column(name = "IdRol", length = 36)
    private String idRol;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;  // ADMIN, PROFESIONAL

    @Column(nullable = false)
    private Byte baja = 0;

    @PrePersist
    protected void onCreate() {
        if (idRol == null) idRol = UUID.randomUUID().toString();
    }
}

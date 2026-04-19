package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "T_Profesion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profesion {

    @Id
    @Column(name = "IdProfesion", length = 36)
    private String idProfesion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Byte baja = 0;

    @PrePersist
    protected void onCreate() {
        if (idProfesion == null) idProfesion = UUID.randomUUID().toString();
    }
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_Rol")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;  // ADMIN, PROFESIONAL

    @Column(nullable = false)
    private Byte baja = 0;
}

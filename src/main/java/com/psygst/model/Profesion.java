package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_Profesion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profesion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProfesion;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Byte baja = 0;
}

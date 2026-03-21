package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_Motivo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Motivo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMotivo;

    @Column(nullable = false, length = 100)
    private String descripcion;  // Alta, Abandono, Derivación, etc.

    @Column(nullable = false)
    private Byte baja = 0;
}

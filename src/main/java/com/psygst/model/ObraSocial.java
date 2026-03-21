package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_ObraSocial")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ObraSocial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idObraSocial;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String contactoLiquidacion;

    @Column(nullable = false)
    private Byte baja = 0;
}

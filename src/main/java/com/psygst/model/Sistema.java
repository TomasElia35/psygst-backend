package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Sistema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSistema;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { fechaCreacion = LocalDateTime.now(); }
}

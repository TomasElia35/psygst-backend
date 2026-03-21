package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Profesional")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profesional {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProfesional;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 20)
    private String cuit;

    @Column(length = 50)
    private String nroLicencia;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String celular;

    @Column(length = 100)
    private String cbu;

    @Column(length = 50)
    private String alias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProfesion")
    private Profesion profesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { fechaModificacion = LocalDateTime.now(); }
}

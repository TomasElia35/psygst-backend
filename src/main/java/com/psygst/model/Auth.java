package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Auth")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Auth {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAuth;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;  // bcrypt hash

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idRol")
    private Rol rol;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProfesional")
    private Profesional profesional;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    @PrePersist
    protected void onCreate() { fechaCreacion = LocalDateTime.now(); }
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Auth")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Auth {

    @Id
    @Column(name = "IdAuth", columnDefinition = "uuid")
    private String idAuth;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;  // bcrypt hash (RN-S02)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdRol")
    private Rol rol;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean baja = false;

    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    @PrePersist
    protected void onCreate() {
        if (idAuth == null) idAuth = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

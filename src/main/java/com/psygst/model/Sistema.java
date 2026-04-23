package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Sistema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sistema {

    @Id
    @Column(name = "IdSistema", columnDefinition = "uuid")
    private String idSistema;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean baja = false;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (idSistema == null) idSistema = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

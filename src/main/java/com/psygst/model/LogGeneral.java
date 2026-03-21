package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_LogGeneral")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLog;

    @Column(length = 36)
    private String idUsuario; // UUID of Auth user

    @Column(nullable = false, length = 100)
    private String accion; // e.g. "VER_HISTORIA_CLINICA"

    @Column(nullable = false, length = 50)
    private String modulo; // e.g. "HISTORIA_CLINICA"

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String datos; // JSON payload

    @Column(length = 50)
    private String ipOrigen;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null)
            timestamp = LocalDateTime.now();
    }
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_HistoriaClinica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriaClinica {

    @Id
    @Column(name = "IdHistoriaClinica", length = 36)
    private String idHistoriaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTurno")
    private Turno turno;

    /**
     * RN-H02: Content stored AES-256-GCM encrypted.
     * EncryptionService handles encrypt on save, decrypt on read.
     * Stored as Base64 encoded ciphertext.
     */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String contenido; // encrypted

    @Column(length = 200)
    private String resumen; // unencrypted short summary for list view

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    /** RN-H03: never physical delete */
    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (idHistoriaClinica == null) idHistoriaClinica = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}

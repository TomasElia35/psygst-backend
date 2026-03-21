package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_HistoriaClinica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistoriaClinica;

    @Column(nullable = false, unique = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTurno")
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
    @JoinColumn(name = "idProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema")
    private Sistema sistema;

    /** RN-H03: never physical delete */
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
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}

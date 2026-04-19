package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Turno")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Turno {

    @Id
    @Column(name = "IdTurno", length = 36)
    private String idTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaComienzo;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 20)
    private String modalidad;  // PRESENCIAL, VIRTUAL

    /**
     * Estados posibles: CONFIRMADO, REALIZADO, CANCELADO
     * RN-T04: REALIZADO es terminal
     * RN-T03: CANCELADO libera el horario
     */
    @Column(nullable = false, length = 20)
    private String estado = "CONFIRMADO";

    /** RN-T06: precio congelado al momento de creación */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObraSocial")
    private ObraSocial obraSocial;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (idTurno == null) idTurno = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { fechaModificacion = LocalDateTime.now(); }
}

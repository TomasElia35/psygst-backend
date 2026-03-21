package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Turno")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Turno {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTurno;

    @Column(nullable = false, unique = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProfesional")
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
    @JoinColumn(name = "idObraSocial")
    private ObraSocial obraSocial;

    @Column(length = 500)
    private String observaciones;

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

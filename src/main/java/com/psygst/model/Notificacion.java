package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificacion;

    @Column(nullable = false, unique = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTurno")
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaciente")
    private Paciente paciente;

    /**
     * Tipo: RECORDATORIO_24HS, CANCELACION, DATOS_PAGO
     */
    @Column(nullable = false, length = 50)
    private String tipo;

    /**
     * Canal: WHATSAPP, EMAIL
     */
    @Column(nullable = false, length = 20)
    private String canal;

    /**
     * Estado: PENDIENTE, ENVIADO, FALLIDO
     * RN-N02: max 3 intentos → FALLIDO
     */
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    /** RN-N02: max 3 retries */
    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(length = 500)
    private String detalle; // error message if FALLIDO

    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @Column(name = "IdNotificacion", columnDefinition = "uuid")
    private String idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTurno")
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPaciente")
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
     * RN-N02: max 3 intentos â†’ FALLIDO
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
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Boolean baja = false;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (idNotificacion == null) idNotificacion = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

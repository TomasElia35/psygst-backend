package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Recibo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recibo {

    @Id
    @Column(name = "IdRecibo", columnDefinition = "uuid")
    private String idRecibo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPago")
    private Pago pago;

    /** RN-F02: REC-{AÃ‘O}-{NNNNN}, correlative per profesional */
    @Column(nullable = false, length = 20)
    private String nroRecibo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(length = 500)
    private String rutaPdf;

    private LocalDateTime fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Boolean baja = false; // RN-F04: anulado, no se reutiliza el nÃºmero

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (idRecibo == null) idRecibo = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

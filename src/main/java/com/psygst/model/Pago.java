package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Pagos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    @Id
    @Column(name = "IdPago", length = 36)
    private String idPago;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTurno", unique = true)
    private Turno turno;

    /** RN-F01: recibo solo si pagado=1 */
    @Column(nullable = false)
    private Boolean pagado = false;

    @Column(length = 50)
    private String metodoPago;  // EFECTIVO, TRANSFERENCIA, etc.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /** RN-F05: comprobante de transferencia opcional */
    @Column(length = 500)
    private String comprobanteImg;

    private LocalDateTime fechaPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (idPago == null) idPago = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
    }
}

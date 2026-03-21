package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_Pagos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @Column(nullable = false, unique = false, length = 36)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTurno", unique = true)
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
    @JoinColumn(name = "idProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSistema")
    private Sistema sistema;

    @Column(nullable = false)
    private Byte baja = 0;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { fechaCreacion = LocalDateTime.now(); }
}

package com.psygst.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_Paciente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Paciente {

    @Id
    @Column(name = "IdPaciente", length = 36)
    private String idPaciente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 20)
    private String dni;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String celular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObraSocial")
    private ObraSocial obraSocial;

    @Column(length = 100)
    private String nroAfiliado;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdProfesional")
    private Profesional profesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSistema")
    private Sistema sistema;

    // Baja lógica
    @Column(nullable = false)
    private Byte baja = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdMotivo")
    private Motivo motivo;  // RN-P03 baja reason

    private LocalDateTime fechaBaja;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (idPaciente == null) idPaciente = UUID.randomUUID().toString();
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { fechaModificacion = LocalDateTime.now(); }
}

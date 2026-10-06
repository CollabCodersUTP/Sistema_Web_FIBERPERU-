package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "orden_trabajo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_solicitud",
            nullable = false,
            unique = true
    )
    private SolicitudInstalacion solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private Coordinador coordinador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    @Column(name = "codigo_orden", nullable = false, unique = true, length = 20)
    private String codigoOrden;

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @Column(name = "hora_programada")
    private LocalTime horaProgramada;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "REGISTRADA";

    @Column(name = "motivo_reprogramacion", length = 500)
    private String motivoReprogramacion;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void antesDeCrear() {
        if (estado == null) {
            estado = "REGISTRADA";
        }
    }
}
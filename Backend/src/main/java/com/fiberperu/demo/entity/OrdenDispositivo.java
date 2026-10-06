package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "orden_dispositivo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_orden_dispositivo",
                        columnNames = {"id_orden", "id_dispositivo"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_dispositivo")
    private Long idOrdenDispositivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenTrabajo orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dispositivo", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_retiro")
    private LocalDateTime fechaRetiro;

    @Builder.Default
    @Column(name = "estado_asignacion", nullable = false, length = 30)
    private String estadoAsignacion = "ASIGNADO";

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void antesDeCrear() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }

        if (estadoAsignacion == null) {
            estadoAsignacion = "ASIGNADO";
        }
    }
}
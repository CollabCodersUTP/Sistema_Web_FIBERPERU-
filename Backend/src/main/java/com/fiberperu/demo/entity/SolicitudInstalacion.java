package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_instalacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudInstalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "codigo_solicitud", nullable = false, unique = true, length = 20)
    private String codigoSolicitud;

    @Column(name = "tipo_servicio", nullable = false, length = 100)
    private String tipoServicio;

    @Column(name = "descripcion_servicio", nullable = false, length = 500)
    private String descripcionServicio;

    @Column(name = "direccion_instalacion", nullable = false, length = 250)
    private String direccionInstalacion;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "REGISTRADA";

    @Column(name = "resultado_evaluacion", length = 30)
    private String resultadoEvaluacion;

    @Column(name = "observacion_evaluacion", length = 500)
    private String observacionEvaluacion;

    @PrePersist
    protected void antesDeCrear() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "REGISTRADA";
        }
    }
}
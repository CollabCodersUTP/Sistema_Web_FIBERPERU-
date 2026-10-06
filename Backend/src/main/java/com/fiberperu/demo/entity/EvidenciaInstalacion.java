package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencia_instalacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaInstalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Long idEvidencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenTrabajo orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coordinador_validador")
    private Coordinador coordinadorValidador;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_archivo", nullable = false, length = 50)
    private String tipoArchivo;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Builder.Default
    @Column(name = "estado_validacion", nullable = false, length = 30)
    private String estadoValidacion = "PENDIENTE";

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(name = "observacion_validacion", length = 500)
    private String observacionValidacion;

    @PrePersist
    protected void antesDeCrear() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }

        if (estadoValidacion == null) {
            estadoValidacion = "PENDIENTE";
        }
    }
}
package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_tecnico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenTrabajo orden;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private String tipoEvento;

    @Column(name = "mensaje", nullable = false, length = 250)
    private String mensaje;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @PrePersist
    protected void antesDeCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

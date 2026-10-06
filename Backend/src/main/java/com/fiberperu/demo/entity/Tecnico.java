package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tecnico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tecnico")
    private Long idTecnico;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Builder.Default
    @Column(name = "disponibilidad", nullable = false)
    private Boolean disponibilidad = true;

    @Builder.Default
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}
package com.fiberperu.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coordinador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coordinador")
    private Long idCoordinador;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Builder.Default
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;
}
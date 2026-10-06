package com.fiberperu.demo.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolResponse {

    private Long idRol;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
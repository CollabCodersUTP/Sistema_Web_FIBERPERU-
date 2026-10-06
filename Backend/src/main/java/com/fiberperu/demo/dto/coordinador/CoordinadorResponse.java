package com.fiberperu.demo.dto.coordinador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinadorResponse {

    private Long idCoordinador;
    private Long idUsuario;

    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;

    private String cargo;
    private Boolean estado;
}
package com.fiberperu.demo.dto.tecnico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoResponse {

    private Long idTecnico;
    private Long idUsuario;

    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;

    private String especialidad;
    private Boolean disponibilidad;
    private Boolean estado;
}
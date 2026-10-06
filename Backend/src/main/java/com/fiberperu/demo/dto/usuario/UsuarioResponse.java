package com.fiberperu.demo.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long idUsuario;

    private Long idRol;
    private String rol;

    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;

    private Boolean estado;
    private LocalDateTime fechaCreacion;
}
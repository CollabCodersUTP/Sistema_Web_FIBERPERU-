package com.fiberperu.demo.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Long idCliente;
    private Long idUsuario;

    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;

    private String tipoDocumento;
    private String numeroDocumento;
    private String razonSocial;
    private String direccion;

    private Boolean estado;
}
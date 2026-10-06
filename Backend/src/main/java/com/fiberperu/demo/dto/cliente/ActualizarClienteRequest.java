package com.fiberperu.demo.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarClienteRequest {
    @NotBlank @Size(max = 100)
    private String nombres;
    @NotBlank @Size(max = 100)
    private String apellidos;
    @NotBlank @Email @Size(max = 150)
    private String correo;
    @Size(max = 20)
    private String telefono;
    @NotBlank @Size(max = 20)
    private String tipoDocumento;
    @NotBlank @Size(max = 20)
    private String numeroDocumento;
    @Size(max = 150)
    private String razonSocial;
    @NotBlank @Size(max = 250)
    private String direccion;
}

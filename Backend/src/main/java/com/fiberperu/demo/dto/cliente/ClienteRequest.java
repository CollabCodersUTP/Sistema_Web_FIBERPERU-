package com.fiberperu.demo.dto.cliente;

import com.fiberperu.demo.dto.usuario.UsuarioRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @Valid
    private UsuarioRequest usuario;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(
            max = 20,
            message = "El tipo de documento no debe superar 20 caracteres"
    )
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(
            max = 20,
            message = "El número de documento no debe superar 20 caracteres"
    )
    private String numeroDocumento;

    @Size(
            max = 150,
            message = "La razón social no debe superar 150 caracteres"
    )
    private String razonSocial;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(
            max = 250,
            message = "La dirección no debe superar 250 caracteres"
    )
    private String direccion;
}
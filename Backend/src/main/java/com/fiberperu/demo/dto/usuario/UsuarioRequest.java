package com.fiberperu.demo.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotNull(message = "El rol es obligatorio")
    private Long idRol;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(
            max = 100,
            message = "Los nombres no deben superar 100 caracteres"
    )
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(
            max = 100,
            message = "Los apellidos no deben superar 100 caracteres"
    )
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(
            max = 150,
            message = "El correo no debe superar 150 caracteres"
    )
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
            min = 8,
            max = 100,
            message = "La contraseña debe contener entre 8 y 100 caracteres"
    )
    private String password;

    @Size(
            max = 20,
            message = "El teléfono no debe superar 20 caracteres"
    )
    private String telefono;
}
package com.fiberperu.demo.dto.coordinador;

import com.fiberperu.demo.dto.usuario.UsuarioRequest;
import jakarta.validation.Valid;
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
public class CoordinadorRequest {

    @Valid
    @NotNull(message = "Los datos del usuario son obligatorios")
    private UsuarioRequest usuario;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(
            max = 100,
            message = "El cargo no debe superar 100 caracteres"
    )
    private String cargo;
}
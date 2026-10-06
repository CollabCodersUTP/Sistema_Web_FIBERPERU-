package com.fiberperu.demo.dto.tecnico;

import com.fiberperu.demo.dto.usuario.UsuarioRequest;
import jakarta.validation.Valid;
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
public class TecnicoRequest {

    @Valid
    @NotNull(message = "Los datos del usuario son obligatorios")
    private UsuarioRequest usuario;

    @Size(
            max = 100,
            message = "La especialidad no debe superar 100 caracteres"
    )
    private String especialidad;
}
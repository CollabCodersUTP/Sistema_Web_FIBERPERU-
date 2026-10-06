package com.fiberperu.demo.dto.orden;

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
public class CambiarEstadoOrdenRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String estadoNuevo;

    @Size(
            max = 500,
            message = "El motivo no debe superar 500 caracteres"
    )
    private String motivo;
}
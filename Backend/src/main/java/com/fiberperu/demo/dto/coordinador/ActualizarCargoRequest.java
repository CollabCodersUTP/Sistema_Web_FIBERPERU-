package com.fiberperu.demo.dto.coordinador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarCargoRequest {

    @NotBlank(message = "El cargo es obligatorio")
    @Size(
            max = 100,
            message = "El cargo no debe superar 100 caracteres"
    )
    private String cargo;
}
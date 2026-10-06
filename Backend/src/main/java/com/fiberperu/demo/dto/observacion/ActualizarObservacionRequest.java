package com.fiberperu.demo.dto.observacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarObservacionRequest {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(
            max = 500,
            message = "La descripción no debe superar 500 caracteres"
    )
    private String descripcion;
}
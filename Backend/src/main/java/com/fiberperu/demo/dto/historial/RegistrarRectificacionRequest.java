package com.fiberperu.demo.dto.historial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrarRectificacionRequest {

    @NotBlank(message = "El detalle de la rectificación es obligatorio")
    @Size(max = 500, message = "El detalle no debe superar 500 caracteres")
    private String detalle;
}

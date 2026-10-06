package com.fiberperu.demo.dto.solicitud;

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
public class EvaluarSolicitudRequest {

    @NotBlank(message = "El resultado de la evaluación es obligatorio")
    private String resultado;

    @Size(
            max = 500,
            message = "La observación no debe superar 500 caracteres"
    )
    private String observacion;
}
package com.fiberperu.demo.dto.evidencia;

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
public class ValidarEvidenciaRequest {

    @NotBlank(message = "El estado de validación es obligatorio")
    private String estadoValidacion;

    @Size(
            max = 500,
            message = "La observación no debe superar 500 caracteres"
    )
    private String observacionValidacion;
}
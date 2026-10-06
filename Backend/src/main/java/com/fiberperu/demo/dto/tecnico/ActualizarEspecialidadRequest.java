package com.fiberperu.demo.dto.tecnico;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarEspecialidadRequest {

    @Size(
            max = 100,
            message = "La especialidad no debe superar 100 caracteres"
    )
    private String especialidad;
}
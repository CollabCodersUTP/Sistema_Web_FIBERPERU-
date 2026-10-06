package com.fiberperu.demo.dto.asignacion;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetirarDispositivoRequest {

    @Size(
            max = 500,
            message = "El motivo no debe superar 500 caracteres"
    )
    private String motivo;
}
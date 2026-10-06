package com.fiberperu.demo.dto.observacion;

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
public class ObservacionServicioRequest {

    @NotNull(message = "La orden es obligatoria")
    private Long idOrden;

    @NotNull(message = "El técnico es obligatorio")
    private Long idTecnico;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(
            max = 500,
            message = "La descripción no debe superar 500 caracteres"
    )
    private String descripcion;
}
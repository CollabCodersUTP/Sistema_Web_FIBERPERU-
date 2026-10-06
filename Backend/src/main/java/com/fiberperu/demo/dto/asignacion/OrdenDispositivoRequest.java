package com.fiberperu.demo.dto.asignacion;

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
public class OrdenDispositivoRequest {

    @NotNull(message = "La orden es obligatoria")
    private Long idOrden;

    @NotNull(message = "El dispositivo es obligatorio")
    private Long idDispositivo;

    @Size(
            max = 500,
            message = "Las observaciones no deben superar 500 caracteres"
    )
    private String observaciones;
}
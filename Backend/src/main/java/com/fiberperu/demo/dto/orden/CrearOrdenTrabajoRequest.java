package com.fiberperu.demo.dto.orden;

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
public class CrearOrdenTrabajoRequest {

    @NotNull(message = "La solicitud es obligatoria")
    private Long idSolicitud;

    private Long idCoordinador;

    @NotBlank(message = "El código de orden es obligatorio")
    @Size(
            max = 20,
            message = "El código de orden no debe superar 20 caracteres"
    )
    private String codigoOrden;

    @Size(
            max = 500,
            message = "Las observaciones no deben superar 500 caracteres"
    )
    private String observaciones;
}

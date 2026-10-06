package com.fiberperu.demo.dto.solicitud;

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
public class SolicitudInstalacionRequest {

    private Long idCliente;

    @NotBlank(message = "El código de solicitud es obligatorio")
    @Size(
            max = 20,
            message = "El código de solicitud no debe superar 20 caracteres"
    )
    private String codigoSolicitud;

    @NotBlank(message = "El tipo de servicio es obligatorio")
    @Size(
            max = 100,
            message = "El tipo de servicio no debe superar 100 caracteres"
    )
    private String tipoServicio;

    @NotBlank(message = "La descripción del servicio es obligatoria")
    @Size(
            max = 500,
            message = "La descripción no debe superar 500 caracteres"
    )
    private String descripcionServicio;

    @NotBlank(message = "La dirección de instalación es obligatoria")
    @Size(
            max = 250,
            message = "La dirección no debe superar 250 caracteres"
    )
    private String direccionInstalacion;
}

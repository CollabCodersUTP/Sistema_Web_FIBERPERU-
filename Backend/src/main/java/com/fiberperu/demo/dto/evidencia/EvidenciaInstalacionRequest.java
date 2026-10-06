package com.fiberperu.demo.dto.evidencia;

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
public class EvidenciaInstalacionRequest {

    @NotNull(message = "La orden es obligatoria")
    private Long idOrden;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(
            max = 255,
            message = "El nombre del archivo no debe superar 255 caracteres"
    )
    private String nombreArchivo;

    @NotBlank(message = "El tipo de archivo es obligatorio")
    @Size(
            max = 50,
            message = "El tipo de archivo no debe superar 50 caracteres"
    )
    private String tipoArchivo;

    @NotBlank(message = "La URL del archivo es obligatoria")
    @Size(
            max = 500,
            message = "La URL no debe superar 500 caracteres"
    )
    private String urlArchivo;

    @Size(
            max = 250,
            message = "La descripción no debe superar 250 caracteres"
    )
    private String descripcion;
}
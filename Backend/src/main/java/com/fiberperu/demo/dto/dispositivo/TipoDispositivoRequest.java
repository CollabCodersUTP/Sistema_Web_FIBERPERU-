package com.fiberperu.demo.dto.dispositivo;

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
public class TipoDispositivoRequest {

    @NotBlank(message = "El nombre del tipo es obligatorio")
    @Size(
            max = 100,
            message = "El nombre no debe superar 100 caracteres"
    )
    private String nombre;

    @Size(
            max = 250,
            message = "La descripción no debe superar 250 caracteres"
    )
    private String descripcion;
}
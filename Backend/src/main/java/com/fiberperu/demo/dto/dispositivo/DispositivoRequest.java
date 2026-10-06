package com.fiberperu.demo.dto.dispositivo;

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
public class DispositivoRequest {

    @NotNull(message = "El tipo de dispositivo es obligatorio")
    private Long idTipoDispositivo;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 80, message = "La marca no debe superar 80 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100, message = "El modelo no debe superar 100 caracteres")
    private String modelo;

    @NotBlank(message = "El número de serie es obligatorio")
    @Size(max = 100, message = "El número de serie no debe superar 100 caracteres")
    private String numeroSerie;
}
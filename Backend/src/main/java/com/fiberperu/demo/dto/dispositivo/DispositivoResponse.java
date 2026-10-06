package com.fiberperu.demo.dto.dispositivo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoResponse {

    private Long idDispositivo;
    private Long idTipoDispositivo;
    private String tipoDispositivo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String estado;
    private Boolean disponibilidad;
    private LocalDateTime fechaRegistro;
}
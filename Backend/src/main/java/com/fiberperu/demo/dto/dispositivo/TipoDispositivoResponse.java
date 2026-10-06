package com.fiberperu.demo.dto.dispositivo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoDispositivoResponse {

    private Long idTipoDispositivo;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
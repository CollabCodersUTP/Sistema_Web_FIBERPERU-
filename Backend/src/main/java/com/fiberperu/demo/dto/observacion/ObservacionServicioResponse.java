package com.fiberperu.demo.dto.observacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservacionServicioResponse {

    private Long idObservacion;

    private Long idOrden;
    private String codigoOrden;

    private Long idTecnico;
    private String nombreTecnico;

    private String descripcion;
    private LocalDateTime fechaRegistro;
    private Boolean estado;
}
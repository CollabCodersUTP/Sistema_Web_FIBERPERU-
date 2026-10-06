package com.fiberperu.demo.dto.asignacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDispositivoResponse {

    private Long idOrdenDispositivo;

    private Long idOrden;
    private String codigoOrden;

    private Long idDispositivo;
    private String numeroSerie;
    private String tipoDispositivo;
    private String marca;
    private String modelo;

    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaRetiro;

    private String estadoAsignacion;
    private String observaciones;
}
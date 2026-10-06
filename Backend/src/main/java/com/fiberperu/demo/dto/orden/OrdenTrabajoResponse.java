package com.fiberperu.demo.dto.orden;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoResponse {

    private Long idOrden;
    private String codigoOrden;

    private Long idSolicitud;
    private String codigoSolicitud;

    private Long idCoordinador;
    private String nombreCoordinador;

    private Long idTecnico;
    private String nombreTecnico;

    private LocalDate fechaProgramada;
    private LocalTime horaProgramada;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;

    private String estado;
    private String motivoReprogramacion;
    private String observaciones;
}
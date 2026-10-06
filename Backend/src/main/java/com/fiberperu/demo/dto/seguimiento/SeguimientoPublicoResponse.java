package com.fiberperu.demo.dto.seguimiento;

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
public class SeguimientoPublicoResponse {

    private String codigoConsultado;
    private String codigoSolicitud;
    private String codigoOrden;
    private String tipoServicio;
    private LocalDateTime fechaSolicitud;
    private LocalDate fechaProgramada;
    private LocalTime horaProgramada;
    private String estadoSolicitud;
    private String estadoOrden;
    private String estadoActual;
    private String etapaActual;
    private String nombreTecnico;
}

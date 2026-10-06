package com.fiberperu.demo.dto.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudInstalacionResponse {

    private Long idSolicitud;
    private Long idCliente;
    private String numeroDocumentoCliente;
    private String nombreCliente;
    private String codigoSolicitud;
    private String tipoServicio;
    private String descripcionServicio;
    private String direccionInstalacion;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String resultadoEvaluacion;
    private String observacionEvaluacion;
}
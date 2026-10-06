package com.fiberperu.demo.dto.evidencia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaInstalacionResponse {

    private Long idEvidencia;

    private Long idOrden;
    private String codigoOrden;

    private Long idCoordinadorValidador;
    private String nombreCoordinadorValidador;

    private String nombreArchivo;
    private String tipoArchivo;
    private String urlArchivo;
    private String descripcion;

    private LocalDateTime fechaRegistro;
    private String estadoValidacion;
    private LocalDateTime fechaValidacion;
    private String observacionValidacion;
}
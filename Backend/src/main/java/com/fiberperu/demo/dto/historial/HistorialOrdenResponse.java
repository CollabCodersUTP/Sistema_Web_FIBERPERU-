package com.fiberperu.demo.dto.historial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialOrdenResponse {

    private Long idHistorial;

    private Long idOrden;
    private String codigoOrden;

    private Long idUsuario;
    private String nombreUsuario;
    private String correoUsuario;

    private String tipoEvento;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaHora;
    private String motivo;
}
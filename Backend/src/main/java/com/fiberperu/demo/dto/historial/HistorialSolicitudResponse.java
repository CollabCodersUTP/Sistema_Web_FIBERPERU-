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
public class HistorialSolicitudResponse {
    private Long idHistorialSolicitud;
    private Long idSolicitud;
    private String codigoSolicitud;
    private Long idUsuario;
    private String nombreUsuario;
    private String tipoEvento;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaHora;
    private String motivo;
}

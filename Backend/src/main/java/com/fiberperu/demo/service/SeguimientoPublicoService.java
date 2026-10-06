package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.seguimiento.SeguimientoPublicoResponse;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.SolicitudInstalacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeguimientoPublicoService {

    private final SolicitudInstalacionRepository solicitudRepository;
    private final OrdenTrabajoRepository ordenRepository;

    public SeguimientoPublicoResponse consultar(String valor) {
        String codigo = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        if (codigo.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar un código de seguimiento");
        }

        SolicitudInstalacion solicitud;
        OrdenTrabajo orden;

        if (codigo.startsWith("SOL-")) {
            solicitud = solicitudRepository.findByCodigoSolicitud(codigo)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Solicitud no encontrada con código: " + codigo
                    ));
            orden = ordenRepository.findBySolicitudIdSolicitud(
                    solicitud.getIdSolicitud()
            ).orElse(null);
        } else if (codigo.startsWith("OT-")) {
            orden = ordenRepository.findByCodigoOrden(codigo)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Orden no encontrada con código: " + codigo
                    ));
            solicitud = orden.getSolicitud();
        } else {
            throw new IllegalArgumentException(
                    "El código debe comenzar con SOL- u OT-"
            );
        }

        String nombreTecnico = null;
        if (orden != null && orden.getTecnico() != null) {
            Usuario usuario = orden.getTecnico().getUsuario();
            nombreTecnico = usuario.getNombres() + " " + usuario.getApellidos();
        }

        return SeguimientoPublicoResponse.builder()
                .codigoConsultado(codigo)
                .codigoSolicitud(solicitud.getCodigoSolicitud())
                .codigoOrden(orden == null ? null : orden.getCodigoOrden())
                .tipoServicio(solicitud.getTipoServicio())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .fechaProgramada(orden == null ? null : orden.getFechaProgramada())
                .horaProgramada(orden == null ? null : orden.getHoraProgramada())
                .estadoSolicitud(solicitud.getEstado())
                .estadoOrden(orden == null ? null : orden.getEstado())
                .estadoActual(orden == null ? solicitud.getEstado() : orden.getEstado())
                .etapaActual(determinarEtapa(solicitud, orden))
                .nombreTecnico(nombreTecnico)
                .build();
    }

    private String determinarEtapa(
            SolicitudInstalacion solicitud,
            OrdenTrabajo orden
    ) {
        if (orden == null) {
            return switch (solicitud.getEstado()) {
                case "APROBADA" -> "Solicitud aprobada, pendiente de generar orden";
                case "RECHAZADA" -> "Solicitud rechazada";
                default -> "Solicitud en evaluación";
            };
        }

        return switch (orden.getEstado()) {
            case "REGISTRADA" -> "Orden generada, pendiente de programación";
            case "PROGRAMADA" -> "Instalación programada";
            case "ASIGNADA" -> "Técnico asignado";
            case "EN_PROCESO" -> "Instalación en ejecución";
            case "OBSERVADA" -> "Instalación observada, requiere subsanación";
            case "FINALIZADA" -> "Instalación finalizada";
            case "CANCELADA" -> "Orden cancelada";
            default -> "Seguimiento disponible";
        };
    }
}

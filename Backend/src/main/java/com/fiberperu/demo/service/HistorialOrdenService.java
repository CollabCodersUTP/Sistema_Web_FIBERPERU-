package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.historial.HistorialOrdenResponse;
import com.fiberperu.demo.entity.HistorialOrden;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialOrdenService {

    private final HistorialOrdenRepository historialRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;

    /**
     * Lista todos los eventos históricos.
     * Se reserva para auditoría administrativa.
     */
    public List<HistorialOrdenResponse> listarTodos() {
        return historialRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Obtiene el historial cronológico de una orden.
     */
    public List<HistorialOrdenResponse> listarPorOrden(
            Long idOrden
    ) {
        if (!ordenTrabajoRepository.existsById(idOrden)) {
            throw new RecursoNoEncontradoException(
                    "Orden no encontrada con id: " + idOrden
            );
        }

        return historialRepository
                .findByOrdenIdOrdenOrderByFechaHoraAsc(idOrden)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un evento histórico por su identificador.
     */
    public HistorialOrdenResponse buscarResponsePorId(
            Long idHistorial
    ) {
        HistorialOrden historial =
                historialRepository.findById(idHistorial)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Evento histórico no encontrado "
                                                + "con id: "
                                                + idHistorial
                                )
                        );

        return convertirAResponse(historial);
    }

    /**
     * Convierte la entidad en una respuesta segura.
     */
    public HistorialOrdenResponse convertirAResponse(
            HistorialOrden historial
    ) {
        OrdenTrabajo orden = historial.getOrden();
        Usuario usuario = historial.getUsuario();

        String nombreUsuario =
                usuario.getNombres()
                        + " "
                        + usuario.getApellidos();

        return HistorialOrdenResponse.builder()
                .idHistorial(
                        historial.getIdHistorial()
                )
                .idOrden(orden.getIdOrden())
                .codigoOrden(orden.getCodigoOrden())
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(nombreUsuario)
                .correoUsuario(usuario.getCorreo())
                .tipoEvento(historial.getTipoEvento())
                .estadoAnterior(
                        historial.getEstadoAnterior()
                )
                .estadoNuevo(
                        historial.getEstadoNuevo()
                )
                .fechaHora(historial.getFechaHora())
                .motivo(historial.getMotivo())
                .build();
    }
}
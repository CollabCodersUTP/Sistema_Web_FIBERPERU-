package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.observacion.ObservacionServicioRequest;
import com.fiberperu.demo.dto.observacion.ObservacionServicioResponse;
import com.fiberperu.demo.entity.ObservacionServicio;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.ObservacionServicioRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObservacionServicioService {

    private final ObservacionServicioRepository observacionRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final TecnicoRepository tecnicoRepository;

    /**
     * Lista todas las observaciones activas e inactivas.
     */
    public List<ObservacionServicioResponse> listarTodas() {
        return observacionRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista las observaciones correspondientes a una orden.
     */
    public List<ObservacionServicioResponse> listarPorOrden(
            Long idOrden
    ) {
        return observacionRepository
                .findByOrdenIdOrden(idOrden)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca una observación por su identificador.
     */
    public ObservacionServicioResponse buscarResponsePorId(
            Long idObservacion
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idObservacion)
        );
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public ObservacionServicio buscarEntidadPorId(
            Long idObservacion
    ) {
        return observacionRepository.findById(idObservacion)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Observación no encontrada con id: "
                                        + idObservacion
                        )
                );
    }

    /**
     * Registra una observación técnica para una orden.
     */
    @Transactional
    public ObservacionServicioResponse registrar(
            ObservacionServicioRequest request
    ) {
        OrdenTrabajo orden = ordenTrabajoRepository
                .findById(request.getIdOrden())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden no encontrada con id: "
                                        + request.getIdOrden()
                        )
                );

        validarOrdenParaObservacion(orden);

        Tecnico tecnico = tecnicoRepository
                .findById(request.getIdTecnico())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Técnico no encontrado con id: "
                                        + request.getIdTecnico()
                        )
                );

        if (!Boolean.TRUE.equals(tecnico.getEstado())) {
            throw new IllegalStateException(
                    "El técnico se encuentra inactivo"
            );
        }

        /*
         * Si la orden ya tiene un técnico asignado,
         * únicamente ese técnico puede registrar observaciones.
         */
        if (orden.getTecnico() != null
                && !orden.getTecnico()
                .getIdTecnico()
                .equals(tecnico.getIdTecnico())) {
            throw new IllegalStateException(
                    "El técnico indicado no está asignado a la orden"
            );
        }

        ObservacionServicio observacion =
                ObservacionServicio.builder()
                        .orden(orden)
                        .tecnico(tecnico)
                        .descripcion(
                                request.getDescripcion().trim()
                        )
                        .estado(true)
                        .build();

        ObservacionServicio guardada =
                observacionRepository.save(observacion);

        return convertirAResponse(guardada);
    }

    /**
     * Actualiza el contenido de una observación activa.
     */
    @Transactional
    public ObservacionServicioResponse actualizarDescripcion(
            Long idObservacion,
            String descripcion
    ) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción es obligatoria"
            );
        }

        if (descripcion.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "La descripción no debe superar 500 caracteres"
            );
        }

        ObservacionServicio observacion =
                buscarEntidadPorId(idObservacion);

        if (!Boolean.TRUE.equals(observacion.getEstado())) {
            throw new IllegalStateException(
                    "No se puede actualizar una observación inactiva"
            );
        }

        observacion.setDescripcion(descripcion.trim());

        ObservacionServicio actualizada =
                observacionRepository.save(observacion);

        return convertirAResponse(actualizada);
    }

    /**
     * Activa o desactiva una observación.
     */
    @Transactional
    public ObservacionServicioResponse cambiarEstado(
            Long idObservacion,
            boolean estado
    ) {
        ObservacionServicio observacion =
                buscarEntidadPorId(idObservacion);

        observacion.setEstado(estado);

        ObservacionServicio actualizada =
                observacionRepository.save(observacion);

        return convertirAResponse(actualizada);
    }

    /**
     * Convierte la entidad en una respuesta controlada.
     */
    public ObservacionServicioResponse convertirAResponse(
            ObservacionServicio observacion
    ) {
        OrdenTrabajo orden = observacion.getOrden();
        Tecnico tecnico = observacion.getTecnico();
        Usuario usuarioTecnico = tecnico.getUsuario();

        String nombreTecnico =
                usuarioTecnico.getNombres()
                        + " "
                        + usuarioTecnico.getApellidos();

        return ObservacionServicioResponse.builder()
                .idObservacion(
                        observacion.getIdObservacion()
                )
                .idOrden(orden.getIdOrden())
                .codigoOrden(orden.getCodigoOrden())
                .idTecnico(tecnico.getIdTecnico())
                .nombreTecnico(nombreTecnico)
                .descripcion(observacion.getDescripcion())
                .fechaRegistro(
                        observacion.getFechaRegistro()
                )
                .estado(observacion.getEstado())
                .build();
    }

    private void validarOrdenParaObservacion(
            OrdenTrabajo orden
    ) {
        if ("CANCELADA".equals(orden.getEstado())
                || "FINALIZADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden registrar observaciones "
                            + "en una orden cerrada"
            );
        }

        if (orden.getTecnico() == null) {
            throw new IllegalStateException(
                    "La orden debe tener un técnico asignado"
            );
        }
    }
}
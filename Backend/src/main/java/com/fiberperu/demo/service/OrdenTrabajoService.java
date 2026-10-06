package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.orden.*;
import com.fiberperu.demo.entity.*;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenTrabajoService {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(
            "REGISTRADA",
            "PROGRAMADA",
            "ASIGNADA",
            "EN_PROCESO",
            "OBSERVADA",
            "FINALIZADA",
            "CANCELADA"
    );

    private final OrdenTrabajoRepository ordenRepository;
    private final SolicitudInstalacionRepository solicitudRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialOrdenRepository historialRepository;
    private final OrdenDispositivoRepository ordenDispositivoRepository;
    private final EvidenciaInstalacionRepository evidenciaRepository;

    public List<OrdenTrabajoResponse> listarTodas() {
        return ordenRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /** Devuelve únicamente las órdenes asignadas al técnico autenticado. */
    public List<OrdenTrabajoResponse> listarMisOrdenes(
            String correoTecnico
    ) {
        Usuario usuario = buscarUsuarioPorCorreo(correoTecnico);

        Tecnico tecnico = tecnicoRepository
                .findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario autenticado no posee un perfil de técnico"
                ));

        return ordenRepository.findByTecnicoIdTecnico(tecnico.getIdTecnico())
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public OrdenTrabajoResponse buscarResponsePorId(Long id) {
        return convertirAResponse(buscarEntidadPorId(id));
    }

    public OrdenTrabajoResponse buscarResponsePorCodigo(
            String codigo
    ) {
        OrdenTrabajo orden = ordenRepository.findByCodigoOrden(codigo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden no encontrada con código: " + codigo
                        )
                );

        return convertirAResponse(orden);
    }

    public OrdenTrabajo buscarEntidadPorId(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden no encontrada con id: " + id
                        )
                );
    }

    @Transactional
    public OrdenTrabajoResponse crear(
            CrearOrdenTrabajoRequest request,
            String correoResponsable
    ) {
        if (ordenRepository.findByCodigoOrden(
                request.getCodigoOrden().trim()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "El código de orden ya está registrado"
            );
        }

        SolicitudInstalacion solicitud =
                solicitudRepository.findById(request.getIdSolicitud())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Solicitud no encontrada con id: "
                                                + request.getIdSolicitud()
                                )
                        );

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new IllegalStateException(
                    "Solo una solicitud aprobada puede generar una orden"
            );
        }

        if (ordenRepository.findBySolicitudIdSolicitud(
                solicitud.getIdSolicitud()
        ).isPresent()) {
            throw new IllegalStateException(
                    "La solicitud ya tiene una orden de trabajo"
            );
        }

        Usuario usuarioResponsable = buscarUsuarioPorCorreo(correoResponsable);
        Coordinador coordinador;
        if (request.getIdCoordinador() == null) {
            coordinador = coordinadorRepository
                    .findByUsuarioIdUsuario(usuarioResponsable.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El administrador debe seleccionar un coordinador"
                    ));
        } else {
            coordinador = coordinadorRepository.findById(request.getIdCoordinador())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Coordinador no encontrado con id: "
                                    + request.getIdCoordinador()
                    ));
        }

        if (!Boolean.TRUE.equals(coordinador.getEstado())) {
            throw new IllegalStateException(
                    "El coordinador no se encuentra activo"
            );
        }

        OrdenTrabajo orden = OrdenTrabajo.builder()
                .solicitud(solicitud)
                .coordinador(coordinador)
                .codigoOrden(request.getCodigoOrden().trim())
                .estado("REGISTRADA")
                .observaciones(
                        normalizarTextoOpcional(
                                request.getObservaciones()
                        )
                )
                .build();

        OrdenTrabajo guardada = ordenRepository.save(orden);

        registrarHistorial(
                guardada,
                usuarioResponsable,
                "CREACION",
                null,
                "REGISTRADA",
                "Creación de la orden de trabajo"
        );

        return convertirAResponse(guardada);
    }

    @Transactional
    public OrdenTrabajoResponse asignarTecnico(
            Long idOrden,
            AsignarTecnicoRequest request,
            String correoResponsable
    ) {
        OrdenTrabajo orden = buscarEntidadPorId(idOrden);

        Tecnico tecnico = tecnicoRepository.findById(
                request.getIdTecnico()
        ).orElseThrow(() ->
                new RecursoNoEncontradoException(
                        "Técnico no encontrado con id: "
                                + request.getIdTecnico()
                )
        );

        if (!Boolean.TRUE.equals(tecnico.getEstado())) {
            throw new IllegalStateException(
                    "El técnico no se encuentra activo"
            );
        }

        if (!Boolean.TRUE.equals(tecnico.getDisponibilidad())) {
            throw new IllegalStateException(
                    "El técnico no se encuentra disponible"
            );
        }

        if (orden.getFechaProgramada() != null
                && orden.getHoraProgramada() != null
                && ordenRepository.existeConflictoHorario(
                        tecnico.getIdTecnico(),
                        orden.getFechaProgramada(),
                        orden.getHoraProgramada(),
                        orden.getIdOrden()
                )) {
            throw new IllegalStateException(
                    "El técnico ya tiene una orden programada en ese horario"
            );
        }

        if ("FINALIZADA".equals(orden.getEstado())
                || "CANCELADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se puede asignar un técnico a una orden cerrada"
            );
        }

        Usuario usuarioResponsable =
                buscarUsuarioPorCorreo(correoResponsable);

        String estadoAnterior = orden.getEstado();

        orden.setTecnico(tecnico);
        orden.setEstado("ASIGNADA");

        OrdenTrabajo actualizada =
                ordenRepository.save(orden);

        registrarHistorial(
                actualizada,
                usuarioResponsable,
                "ASIGNACION_TECNICO",
                estadoAnterior,
                "ASIGNADA",
                "Asignación de técnico a la orden"
        );

        return convertirAResponse(actualizada);
    }

    @Transactional
    public OrdenTrabajoResponse programar(
            Long idOrden,
            ProgramarOrdenRequest request,
            String correoResponsable
    ) {
        OrdenTrabajo orden = buscarEntidadPorId(idOrden);

        if ("FINALIZADA".equals(orden.getEstado())
                || "CANCELADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se puede programar una orden cerrada"
            );
        }

        if (orden.getTecnico() != null
                && ordenRepository.existeConflictoHorario(
                        orden.getTecnico().getIdTecnico(),
                        request.getFechaProgramada(),
                        request.getHoraProgramada(),
                        orden.getIdOrden()
                )) {
            throw new IllegalStateException(
                    "El técnico ya tiene una orden programada en ese horario"
            );
        }

        Usuario usuarioResponsable =
                buscarUsuarioPorCorreo(correoResponsable);

        String estadoAnterior = orden.getEstado();

        orden.setFechaProgramada(request.getFechaProgramada());
        orden.setHoraProgramada(request.getHoraProgramada());
        orden.setEstado("PROGRAMADA");

        OrdenTrabajo actualizada =
                ordenRepository.save(orden);

        registrarHistorial(
                actualizada,
                usuarioResponsable,
                "PROGRAMACION",
                estadoAnterior,
                "PROGRAMADA",
                "Programación de la instalación"
        );

        return convertirAResponse(actualizada);
    }

    @Transactional
    public OrdenTrabajoResponse cambiarEstado(
            Long idOrden,
            CambiarEstadoOrdenRequest request,
            String correoResponsable,
            boolean puedeCerrar
    ) {
        OrdenTrabajo orden = buscarEntidadPorId(idOrden);

        String estadoNuevo = request.getEstadoNuevo()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ESTADOS_PERMITIDOS.contains(estadoNuevo)) {
            throw new IllegalArgumentException(
                    "Estado de orden no permitido: " + estadoNuevo
            );
        }

        if (estadoNuevo.equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "La orden ya se encuentra en el estado "
                            + estadoNuevo
            );
        }

        if ("CANCELADA".equals(orden.getEstado())
                || "FINALIZADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se puede modificar una orden cerrada"
            );
        }

        if ("EN_PROCESO".equals(estadoNuevo)
                && orden.getTecnico() == null) {
            throw new IllegalStateException(
                    "La orden debe tener un técnico asignado"
            );
        }

        if ("FINALIZADA".equals(estadoNuevo)) {
            if (!puedeCerrar) {
                throw new IllegalStateException(
                        "Solo un coordinador o administrador puede cerrar la orden"
                );
            }
            validarCierreConforme(orden);
        }

        Usuario usuarioResponsable =
                buscarUsuarioPorCorreo(correoResponsable);

        String estadoAnterior = orden.getEstado();

        orden.setEstado(estadoNuevo);

        if ("EN_PROCESO".equals(estadoNuevo)
                && orden.getFechaInicio() == null) {
            orden.setFechaInicio(LocalDateTime.now());
        }

        if ("FINALIZADA".equals(estadoNuevo)) {
            orden.setFechaFinalizacion(LocalDateTime.now());
        }

        if ("PROGRAMADA".equals(estadoNuevo)
                && request.getMotivo() != null) {
            orden.setMotivoReprogramacion(
                    normalizarTextoOpcional(request.getMotivo())
            );
        }

        OrdenTrabajo actualizada =
                ordenRepository.save(orden);

        registrarHistorial(
                actualizada,
                usuarioResponsable,
                "CAMBIO_ESTADO",
                estadoAnterior,
                estadoNuevo,
                normalizarTextoOpcional(request.getMotivo())
        );

        return convertirAResponse(actualizada);
    }

    private void validarCierreConforme(OrdenTrabajo orden) {
        List<OrdenDispositivo> equipos = ordenDispositivoRepository
                .findByOrdenIdOrden(orden.getIdOrden());
        if (equipos.isEmpty() || equipos.stream().noneMatch(equipo ->
                "INSTALADO".equals(equipo.getEstadoAsignacion())
        )) {
            throw new IllegalStateException(
                    "La orden debe tener al menos un dispositivo instalado"
            );
        }

        List<EvidenciaInstalacion> evidencias = evidenciaRepository
                .findByOrdenIdOrden(orden.getIdOrden());
        if (evidencias.isEmpty() || evidencias.stream().anyMatch(evidencia ->
                !"APROBADA".equals(evidencia.getEstadoValidacion())
        )) {
            throw new IllegalStateException(
                    "Todas las evidencias deben estar aprobadas antes del cierre"
            );
        }
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario autenticado no encontrado: "
                                        + correo
                        )
                );
    }

    private void registrarHistorial(
            OrdenTrabajo orden,
            Usuario usuario,
            String tipoEvento,
            String estadoAnterior,
            String estadoNuevo,
            String motivo
    ) {
        HistorialOrden historial = HistorialOrden.builder()
                .orden(orden)
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .motivo(motivo)
                .build();

        historialRepository.save(historial);
    }

    private OrdenTrabajoResponse convertirAResponse(
            OrdenTrabajo orden
    ) {
        SolicitudInstalacion solicitud = orden.getSolicitud();
        Coordinador coordinador = orden.getCoordinador();
        Usuario usuarioCoordinador = coordinador.getUsuario();

        Long idTecnico = null;
        String nombreTecnico = null;

        if (orden.getTecnico() != null) {
            Tecnico tecnico = orden.getTecnico();
            Usuario usuarioTecnico = tecnico.getUsuario();

            idTecnico = tecnico.getIdTecnico();
            nombreTecnico = construirNombreCompleto(
                    usuarioTecnico
            );
        }

        return OrdenTrabajoResponse.builder()
                .idOrden(orden.getIdOrden())
                .codigoOrden(orden.getCodigoOrden())
                .idSolicitud(solicitud.getIdSolicitud())
                .codigoSolicitud(
                        solicitud.getCodigoSolicitud()
                )
                .idCoordinador(
                        coordinador.getIdCoordinador()
                )
                .nombreCoordinador(
                        construirNombreCompleto(
                                usuarioCoordinador
                        )
                )
                .idTecnico(idTecnico)
                .nombreTecnico(nombreTecnico)
                .fechaProgramada(
                        orden.getFechaProgramada()
                )
                .horaProgramada(
                        orden.getHoraProgramada()
                )
                .fechaInicio(orden.getFechaInicio())
                .fechaFinalizacion(
                        orden.getFechaFinalizacion()
                )
                .estado(orden.getEstado())
                .motivoReprogramacion(
                        orden.getMotivoReprogramacion()
                )
                .observaciones(orden.getObservaciones())
                .build();
    }

    private String construirNombreCompleto(Usuario usuario) {
        return usuario.getNombres()
                + " "
                + usuario.getApellidos();
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}

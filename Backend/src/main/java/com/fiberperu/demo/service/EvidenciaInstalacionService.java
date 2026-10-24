package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionRequest;
import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionResponse;
import com.fiberperu.demo.dto.evidencia.ValidarEvidenciaRequest;
import com.fiberperu.demo.entity.Coordinador;
import com.fiberperu.demo.entity.EvidenciaInstalacion;
import com.fiberperu.demo.entity.HistorialOrden;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.CoordinadorRepository;
import com.fiberperu.demo.repository.EvidenciaInstalacionRepository;
import com.fiberperu.demo.repository.HistorialOrdenRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidenciaInstalacionService {

    public record ArchivoEvidencia(byte[] contenido, String nombre, String tipo) {}

    private static final Set<String> ESTADOS_VALIDACION =
            Set.of(
                    "APROBADA",
                    "OBSERVADA",
                    "RECHAZADA"
            );

    private final EvidenciaInstalacionRepository evidenciaRepository;
    private final HistorialOrdenRepository historialRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final TecnicoRepository tecnicoRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    /**
     * Lista todas las evidencias registradas.
     */
    public List<EvidenciaInstalacionResponse> listarTodas() {
        return evidenciaRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista las evidencias asociadas a una orden.
     */
    public List<EvidenciaInstalacionResponse> listarPorOrden(
            Long idOrden
    ) {
        return evidenciaRepository
                .findByOrdenIdOrden(idOrden)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista las evidencias pendientes de validación.
     */
    public List<EvidenciaInstalacionResponse> listarPendientes() {
        return evidenciaRepository
                .findByEstadoValidacion("PENDIENTE")
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca una evidencia por su identificador.
     */
    public EvidenciaInstalacionResponse buscarResponsePorId(
            Long idEvidencia
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idEvidencia)
        );
    }

    public ArchivoEvidencia obtenerArchivo(
            Long idEvidencia, String correoResponsable, boolean accesoGeneral
    ) {
        EvidenciaInstalacion evidencia = buscarEntidadPorId(idEvidencia);
        if (!accesoGeneral) {
            Usuario usuario = usuarioRepository.findByCorreo(correoResponsable)
                    .orElseThrow(() -> new AccessDeniedException(
                            "No tiene permisos para ver esta evidencia"
                    ));
            Tecnico tecnico = tecnicoRepository
                    .findByUsuarioIdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new AccessDeniedException(
                            "No tiene permisos para ver esta evidencia"
                    ));
            if (evidencia.getOrden().getTecnico() == null
                    || !tecnico.getIdTecnico().equals(
                            evidencia.getOrden().getTecnico().getIdTecnico())) {
                throw new AccessDeniedException(
                        "No tiene permisos para ver esta evidencia"
                );
            }
        }

        return new ArchivoEvidencia(
                azureBlobStorageService.leerEvidencia(
                        evidencia.getOrden().getIdOrden(), evidencia.getUrlArchivo()
                ),
                evidencia.getNombreArchivo(),
                evidencia.getTipoArchivo()
        );
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public EvidenciaInstalacion buscarEntidadPorId(
            Long idEvidencia
    ) {
        return evidenciaRepository.findById(idEvidencia)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Evidencia no encontrada con id: "
                                        + idEvidencia
                        )
                );
    }

    /**
     * Registra una evidencia con estado PENDIENTE.
     */
    @Transactional
    public EvidenciaInstalacionResponse registrar(
            EvidenciaInstalacionRequest request
    ) {
        OrdenTrabajo orden = ordenTrabajoRepository
                .findById(request.getIdOrden())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden no encontrada con id: "
                                        + request.getIdOrden()
                        )
                );

        validarOrdenParaEvidencia(orden);

        EvidenciaInstalacion evidencia =
                EvidenciaInstalacion.builder()
                        .orden(orden)
                        .coordinadorValidador(null)
                        .nombreArchivo(
                                request.getNombreArchivo().trim()
                        )
                        .tipoArchivo(
                                request.getTipoArchivo()
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
                        )
                        .urlArchivo(
                                request.getUrlArchivo().trim()
                        )
                        .descripcion(
                                normalizarTextoOpcional(
                                        request.getDescripcion()
                                )
                        )
                        .estadoValidacion("PENDIENTE")
                        .fechaValidacion(null)
                        .observacionValidacion(null)
                        .build();

        EvidenciaInstalacion guardada =
                evidenciaRepository.save(evidencia);

        return convertirAResponse(guardada);
    }

    /**
     * Sube el archivo a Azure y registra la evidencia. Un técnico solo puede
     * adjuntar archivos en una orden que le fue asignada.
     */
    @Transactional
    public EvidenciaInstalacionResponse registrarArchivo(
            Long idOrden,
            MultipartFile archivo,
            String descripcion,
            String correoResponsable,
            boolean esAdministrador
    ) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden no encontrada con id: " + idOrden
                ));

        if (!esAdministrador) {
            Usuario usuario = usuarioRepository.findByCorreo(correoResponsable)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Usuario autenticado no encontrado: " + correoResponsable
                    ));
            Tecnico tecnico = tecnicoRepository
                    .findByUsuarioIdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalStateException(
                            "El usuario autenticado no posee un perfil de técnico"
                    ));

            if (orden.getTecnico() == null
                    || !orden.getTecnico().getIdTecnico().equals(tecnico.getIdTecnico())) {
                throw new IllegalStateException(
                        "Solo el técnico asignado puede registrar evidencias de esta orden"
                );
            }
        }

        validarOrdenParaEvidencia(orden);
        String urlArchivo = azureBlobStorageService.almacenarEvidencia(idOrden, archivo);

        EvidenciaInstalacionRequest request = EvidenciaInstalacionRequest.builder()
                .idOrden(idOrden)
                .nombreArchivo(archivo.getOriginalFilename())
                .tipoArchivo(archivo.getContentType())
                .urlArchivo(urlArchivo)
                .descripcion(descripcion)
                .build();

        return registrar(request);
    }

    /**
     * Reenvía una evidencia observada con un archivo corregido. El mismo
     * registro vuelve a PENDIENTE para que el coordinador lo revise otra vez.
     */
    @Transactional
    public EvidenciaInstalacionResponse reenviarArchivo(
            Long idEvidencia,
            MultipartFile archivo,
            String descripcion,
            String correoResponsable,
            boolean esAdministrador
    ) {
        EvidenciaInstalacion evidencia = buscarEntidadPorId(idEvidencia);
        if (!"OBSERVADA".equals(evidencia.getEstadoValidacion())) {
            throw new IllegalStateException(
                    "Solo se puede reenviar una evidencia observada"
            );
        }

        OrdenTrabajo orden = evidencia.getOrden();
        validarOrdenParaEvidencia(orden);

        Usuario usuario = usuarioRepository.findByCorreo(correoResponsable)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado: " + correoResponsable
                ));
        if (!esAdministrador) {
            Tecnico tecnico = tecnicoRepository
                    .findByUsuarioIdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalStateException(
                            "El usuario autenticado no posee un perfil de técnico"
                    ));
            if (orden.getTecnico() == null
                    || !orden.getTecnico().getIdTecnico().equals(tecnico.getIdTecnico())) {
                throw new IllegalStateException(
                        "Solo el técnico asignado puede reenviar evidencias de esta orden"
                );
            }
        }

        String descripcionNueva = normalizarTextoOpcional(descripcion);
        if (descripcionNueva != null && descripcionNueva.length() > 250) {
            throw new IllegalArgumentException(
                    "La descripción no debe superar 250 caracteres"
            );
        }

        String urlAnterior = evidencia.getUrlArchivo();
        String urlNueva = azureBlobStorageService
                .almacenarEvidencia(orden.getIdOrden(), archivo);

        Coordinador validadorAnterior = evidencia.getCoordinadorValidador();
        if (validadorAnterior != null) {
            historialRepository.save(HistorialOrden.builder()
                    .orden(orden)
                    .usuario(validadorAnterior.getUsuario())
                    .tipoEvento("VALIDACION_EVIDENCIA")
                    .estadoAnterior("PENDIENTE")
                    .estadoNuevo("OBSERVADA")
                    .fechaHora(evidencia.getFechaValidacion())
                    .motivo(evidencia.getObservacionValidacion())
                    .build());
        }
        String referenciaAnterior = "Evidencia #" + idEvidencia + ": " + urlAnterior;
        historialRepository.save(HistorialOrden.builder()
                .orden(orden)
                .usuario(usuario)
                .tipoEvento("REENVIO_EVIDENCIA")
                .estadoAnterior("OBSERVADA")
                .estadoNuevo("PENDIENTE")
                .motivo(referenciaAnterior.length() <= 500
                        ? referenciaAnterior : urlAnterior)
                .build());

        evidencia.setNombreArchivo(archivo.getOriginalFilename());
        evidencia.setTipoArchivo(archivo.getContentType().toLowerCase(Locale.ROOT));
        evidencia.setUrlArchivo(urlNueva);
        if (descripcionNueva != null) {
            evidencia.setDescripcion(descripcionNueva);
        }
        evidencia.setEstadoValidacion("PENDIENTE");
        evidencia.setCoordinadorValidador(null);
        evidencia.setFechaValidacion(null);
        // El motivo de la revisión previa permanece visible hasta la nueva revisión.

        return convertirAResponse(evidenciaRepository.save(evidencia));
    }

    /**
     * Valida una evidencia y registra al coordinador responsable.
     */
    @Transactional
    public EvidenciaInstalacionResponse validar(
            Long idEvidencia,
            ValidarEvidenciaRequest request,
            String correoResponsable
    ) {
        EvidenciaInstalacion evidencia =
                buscarEntidadPorId(idEvidencia);

        if (!"PENDIENTE".equals(
                evidencia.getEstadoValidacion()
        )) {
            throw new IllegalStateException(
                    "La evidencia ya fue validada"
            );
        }

        String estadoValidacion =
                request.getEstadoValidacion()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!ESTADOS_VALIDACION.contains(
                estadoValidacion
        )) {
            throw new IllegalArgumentException(
                    "Estado de validación no permitido: "
                            + estadoValidacion
            );
        }

        Usuario usuario = usuarioRepository
                .findByCorreo(correoResponsable)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario autenticado no encontrado: "
                                        + correoResponsable
                        )
                );

        Coordinador coordinador =
                coordinadorRepository
                        .findByUsuarioIdUsuario(
                                usuario.getIdUsuario()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario autenticado no posee "
                                                + "un perfil de coordinador"
                                )
                        );

        if (!Boolean.TRUE.equals(
                coordinador.getEstado()
        )) {
            throw new IllegalStateException(
                    "El coordinador se encuentra inactivo"
            );
        }

        String observacion =
                normalizarTextoOpcional(
                        request.getObservacionValidacion()
                );

        if (("OBSERVADA".equals(estadoValidacion)
                || "RECHAZADA".equals(estadoValidacion))
                && observacion == null) {
            throw new IllegalArgumentException(
                    "Debe registrar una observación para el estado "
                            + estadoValidacion
            );
        }

        evidencia.setCoordinadorValidador(coordinador);
        evidencia.setEstadoValidacion(estadoValidacion);
        evidencia.setFechaValidacion(LocalDateTime.now());
        evidencia.setObservacionValidacion(observacion);

        EvidenciaInstalacion actualizada =
                evidenciaRepository.save(evidencia);

        return convertirAResponse(actualizada);
    }

    /**
     * Convierte la entidad en una respuesta segura.
     */
    public EvidenciaInstalacionResponse convertirAResponse(
            EvidenciaInstalacion evidencia
    ) {
        OrdenTrabajo orden = evidencia.getOrden();

        Long idCoordinador = null;
        String nombreCoordinador = null;

        if (evidencia.getCoordinadorValidador() != null) {
            Coordinador coordinador =
                    evidencia.getCoordinadorValidador();

            Usuario usuario = coordinador.getUsuario();

            idCoordinador =
                    coordinador.getIdCoordinador();

            nombreCoordinador =
                    usuario.getNombres()
                            + " "
                            + usuario.getApellidos();
        }

        return EvidenciaInstalacionResponse.builder()
                .idEvidencia(
                        evidencia.getIdEvidencia()
                )
                .idOrden(orden.getIdOrden())
                .codigoOrden(orden.getCodigoOrden())
                .idCoordinadorValidador(
                        idCoordinador
                )
                .nombreCoordinadorValidador(
                        nombreCoordinador
                )
                .nombreArchivo(
                        evidencia.getNombreArchivo()
                )
                .tipoArchivo(
                        evidencia.getTipoArchivo()
                )
                .urlArchivo(
                        evidencia.getUrlArchivo()
                )
                .descripcion(
                        evidencia.getDescripcion()
                )
                .fechaRegistro(
                        evidencia.getFechaRegistro()
                )
                .estadoValidacion(
                        evidencia.getEstadoValidacion()
                )
                .fechaValidacion(
                        evidencia.getFechaValidacion()
                )
                .observacionValidacion(
                        evidencia.getObservacionValidacion()
                )
                .build();
    }

    private void validarOrdenParaEvidencia(
            OrdenTrabajo orden
    ) {
        if ("CANCELADA".equals(orden.getEstado())
                || "FINALIZADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden registrar evidencias "
                            + "en una orden cerrada"
            );
        }

        if (orden.getTecnico() == null) {
            throw new IllegalStateException(
                    "La orden debe tener un técnico asignado"
            );
        }
    }

    private String normalizarTextoOpcional(
            String texto
    ) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}

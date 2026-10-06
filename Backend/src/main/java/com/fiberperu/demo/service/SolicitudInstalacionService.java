package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.solicitud.EvaluarSolicitudRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionResponse;
import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.SolicitudInstalacion;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.SolicitudInstalacionRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudInstalacionService {

    private final SolicitudInstalacionRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public List<SolicitudInstalacionResponse> listarTodas() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<SolicitudInstalacionResponse> listarPorCliente(
            Long idCliente
    ) {
        return solicitudRepository
                .findByClienteIdCliente(idCliente)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /** Devuelve únicamente las solicitudes del cliente autenticado. */
    public List<SolicitudInstalacionResponse> listarMisSolicitudes(
            String correoCliente
    ) {
        Usuario usuario = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado: " + correoCliente
                ));

        Cliente cliente = clienteRepository
                .findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario autenticado no posee un perfil de cliente"
                ));

        return listarPorCliente(cliente.getIdCliente());
    }

    public SolicitudInstalacionResponse buscarResponsePorId(
            Long id
    ) {
        return convertirAResponse(
                buscarEntidadPorId(id)
        );
    }

    public SolicitudInstalacionResponse buscarResponsePorCodigo(
            String codigo
    ) {
        SolicitudInstalacion solicitud =
                solicitudRepository.findByCodigoSolicitud(codigo)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Solicitud no encontrada: " + codigo
                                )
                        );

        return convertirAResponse(solicitud);
    }

    public SolicitudInstalacion buscarEntidadPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Solicitud no encontrada con id: " + id
                        )
                );
    }

    @Transactional
    public SolicitudInstalacionResponse registrar(
            SolicitudInstalacionRequest request,
            String correoResponsable,
            boolean esCliente
    ) {
        if (solicitudRepository
                .findByCodigoSolicitud(request.getCodigoSolicitud())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "El código de solicitud ya está registrado"
            );
        }

        Cliente cliente;
        if (esCliente) {
            Usuario usuario = usuarioRepository.findByCorreo(correoResponsable)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Usuario autenticado no encontrado: " + correoResponsable
                    ));
            cliente = clienteRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalStateException(
                            "El usuario autenticado no posee un perfil de cliente"
                    ));
        } else {
            if (request.getIdCliente() == null) {
                throw new IllegalArgumentException(
                        "El administrador debe seleccionar un cliente"
                );
            }
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Cliente no encontrado con id: " + request.getIdCliente()
                    ));
        }

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new IllegalStateException(
                    "El cliente no se encuentra activo"
            );
        }

        SolicitudInstalacion solicitud =
                SolicitudInstalacion.builder()
                        .cliente(cliente)
                        .codigoSolicitud(
                                request.getCodigoSolicitud().trim()
                        )
                        .tipoServicio(
                                request.getTipoServicio().trim()
                        )
                        .descripcionServicio(
                                request.getDescripcionServicio().trim()
                        )
                        .direccionInstalacion(
                                request.getDireccionInstalacion().trim()
                        )
                        .estado("REGISTRADA")
                        .resultadoEvaluacion(null)
                        .observacionEvaluacion(null)
                        .build();

        SolicitudInstalacion guardada =
                solicitudRepository.save(solicitud);

        return convertirAResponse(guardada);
    }

    @Transactional
    public SolicitudInstalacionResponse evaluar(
            Long id,
            EvaluarSolicitudRequest request
    ) {
        SolicitudInstalacion solicitud =
                buscarEntidadPorId(id);

        String resultado = request.getResultado()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!resultado.equals("APROBADA")
                && !resultado.equals("RECHAZADA")) {
            throw new IllegalArgumentException(
                    "El resultado debe ser APROBADA o RECHAZADA"
            );
        }

        if ("CANCELADA".equals(solicitud.getEstado())) {
            throw new IllegalStateException(
                    "No se puede evaluar una solicitud cancelada"
            );
        }

        solicitud.setResultadoEvaluacion(resultado);
        solicitud.setObservacionEvaluacion(
                normalizarTextoOpcional(request.getObservacion())
        );
        solicitud.setEstado(resultado);

        SolicitudInstalacion actualizada =
                solicitudRepository.save(solicitud);

        return convertirAResponse(actualizada);
    }

    private SolicitudInstalacionResponse convertirAResponse(
            SolicitudInstalacion solicitud
    ) {
        Cliente cliente = solicitud.getCliente();
        Usuario usuario = cliente.getUsuario();

        String nombreCliente =
                usuario.getNombres()
                        + " "
                        + usuario.getApellidos();

        return SolicitudInstalacionResponse.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .idCliente(cliente.getIdCliente())
                .numeroDocumentoCliente(
                        cliente.getNumeroDocumento()
                )
                .nombreCliente(nombreCliente)
                .codigoSolicitud(
                        solicitud.getCodigoSolicitud()
                )
                .tipoServicio(solicitud.getTipoServicio())
                .descripcionServicio(
                        solicitud.getDescripcionServicio()
                )
                .direccionInstalacion(
                        solicitud.getDireccionInstalacion()
                )
                .fechaSolicitud(
                        solicitud.getFechaSolicitud()
                )
                .estado(solicitud.getEstado())
                .resultadoEvaluacion(
                        solicitud.getResultadoEvaluacion()
                )
                .observacionEvaluacion(
                        solicitud.getObservacionEvaluacion()
                )
                .build();
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}

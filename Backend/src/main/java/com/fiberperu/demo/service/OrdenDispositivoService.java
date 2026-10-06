package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.asignacion.OrdenDispositivoRequest;
import com.fiberperu.demo.dto.asignacion.OrdenDispositivoResponse;
import com.fiberperu.demo.dto.asignacion.RetirarDispositivoRequest;
import com.fiberperu.demo.entity.Dispositivo;
import com.fiberperu.demo.entity.OrdenDispositivo;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.TipoDispositivo;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.DispositivoRepository;
import com.fiberperu.demo.repository.OrdenDispositivoRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import com.fiberperu.demo.repository.TecnicoRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenDispositivoService {

    private final OrdenDispositivoRepository ordenDispositivoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final DispositivoRepository dispositivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    /**
     * Lista todas las asignaciones registradas.
     */
    public List<OrdenDispositivoResponse> listarTodas() {
        return ordenDispositivoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista las asignaciones de una orden.
     */
    public List<OrdenDispositivoResponse> listarPorOrden(
            Long idOrden
    ) {
        return ordenDispositivoRepository
                .findByOrdenIdOrden(idOrden)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista el historial de asignaciones de un dispositivo.
     */
    public List<OrdenDispositivoResponse> listarPorDispositivo(
            Long idDispositivo
    ) {
        return ordenDispositivoRepository
                .findByDispositivoIdDispositivo(idDispositivo)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca una asignación mediante su identificador.
     */
    public OrdenDispositivoResponse buscarResponsePorId(
            Long idOrdenDispositivo
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idOrdenDispositivo)
        );
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public OrdenDispositivo buscarEntidadPorId(
            Long idOrdenDispositivo
    ) {
        return ordenDispositivoRepository
                .findById(idOrdenDispositivo)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Asignación no encontrada con id: "
                                        + idOrdenDispositivo
                        )
                );
    }

    /**
     * Asigna un dispositivo disponible a una orden activa.
     */
    @Transactional
    public OrdenDispositivoResponse asignar(
            OrdenDispositivoRequest request,
            String correoResponsable,
            boolean accesoGlobal
    ) {
        OrdenTrabajo orden = ordenTrabajoRepository
                .findById(request.getIdOrden())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Orden no encontrada con id: "
                                        + request.getIdOrden()
                        )
                );

        validarOrdenDisponibleParaAsignacion(orden);
        validarAccesoAOrden(orden, correoResponsable, accesoGlobal);

        Dispositivo dispositivo = dispositivoRepository
                .findById(request.getIdDispositivo())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Dispositivo no encontrado con id: "
                                        + request.getIdDispositivo()
                        )
                );

        if (!Boolean.TRUE.equals(
                dispositivo.getDisponibilidad()
        )) {
            throw new IllegalStateException(
                    "El dispositivo no se encuentra disponible"
            );
        }

        if (!"EN_STOCK".equals(dispositivo.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden asignar dispositivos en stock"
            );
        }

        ordenDispositivoRepository
                .findByDispositivoIdDispositivoAndFechaRetiroIsNull(
                        dispositivo.getIdDispositivo()
                )
                .ifPresent(asignacion -> {
                    throw new IllegalStateException(
                            "El dispositivo ya tiene una asignación activa"
                    );
                });

        OrdenDispositivo asignacion =
                OrdenDispositivo.builder()
                        .orden(orden)
                        .dispositivo(dispositivo)
                        .estadoAsignacion("ASIGNADO")
                        .observaciones(
                                normalizarTextoOpcional(
                                        request.getObservaciones()
                                )
                        )
                        .build();

        dispositivo.setEstado("ASIGNADO");
        dispositivo.setDisponibilidad(false);

        dispositivoRepository.save(dispositivo);

        OrdenDispositivo guardada =
                ordenDispositivoRepository.save(asignacion);

        return convertirAResponse(guardada);
    }

    /**
     * Marca el dispositivo como instalado en la orden.
     */
    @Transactional
    public OrdenDispositivoResponse marcarComoInstalado(
            Long idOrdenDispositivo,
            String correoResponsable,
            boolean accesoGlobal
    ) {
        OrdenDispositivo asignacion =
                buscarEntidadPorId(idOrdenDispositivo);
        validarAccesoAOrden(
                asignacion.getOrden(), correoResponsable, accesoGlobal
        );

        if (asignacion.getFechaRetiro() != null) {
            throw new IllegalStateException(
                    "La asignación ya fue retirada"
            );
        }

        if ("INSTALADO".equals(
                asignacion.getEstadoAsignacion()
        )) {
            throw new IllegalStateException(
                    "El dispositivo ya está marcado como instalado"
            );
        }

        asignacion.setEstadoAsignacion("INSTALADO");

        Dispositivo dispositivo =
                asignacion.getDispositivo();

        dispositivo.setEstado("INSTALADO");
        dispositivo.setDisponibilidad(false);

        dispositivoRepository.save(dispositivo);

        OrdenDispositivo actualizada =
                ordenDispositivoRepository.save(asignacion);

        return convertirAResponse(actualizada);
    }

    /**
     * Retira un dispositivo y lo vuelve a dejar disponible.
     */
    @Transactional
    public OrdenDispositivoResponse retirar(
            Long idOrdenDispositivo,
            RetirarDispositivoRequest request,
            String correoResponsable,
            boolean accesoGlobal
    ) {
        OrdenDispositivo asignacion =
                buscarEntidadPorId(idOrdenDispositivo);
        validarAccesoAOrden(
                asignacion.getOrden(), correoResponsable, accesoGlobal
        );

        if (asignacion.getFechaRetiro() != null
                || "RETIRADO".equals(
                asignacion.getEstadoAsignacion()
        )) {
            throw new IllegalStateException(
                    "El dispositivo ya fue retirado"
            );
        }

        asignacion.setFechaRetiro(LocalDateTime.now());
        asignacion.setEstadoAsignacion("RETIRADO");

        String motivo = normalizarTextoOpcional(
                request.getMotivo()
        );

        if (motivo != null) {
            asignacion.setObservaciones(motivo);
        }

        Dispositivo dispositivo =
                asignacion.getDispositivo();

        dispositivo.setEstado("EN_STOCK");
        dispositivo.setDisponibilidad(true);

        dispositivoRepository.save(dispositivo);

        OrdenDispositivo actualizada =
                ordenDispositivoRepository.save(asignacion);

        return convertirAResponse(actualizada);
    }

    /**
     * Convierte la entidad en una respuesta segura.
     */
    public OrdenDispositivoResponse convertirAResponse(
            OrdenDispositivo asignacion
    ) {
        OrdenTrabajo orden = asignacion.getOrden();
        Dispositivo dispositivo =
                asignacion.getDispositivo();
        TipoDispositivo tipo =
                dispositivo.getTipoDispositivo();

        return OrdenDispositivoResponse.builder()
                .idOrdenDispositivo(
                        asignacion.getIdOrdenDispositivo()
                )
                .idOrden(orden.getIdOrden())
                .codigoOrden(orden.getCodigoOrden())
                .idDispositivo(
                        dispositivo.getIdDispositivo()
                )
                .numeroSerie(
                        dispositivo.getNumeroSerie()
                )
                .tipoDispositivo(tipo.getNombre())
                .marca(dispositivo.getMarca())
                .modelo(dispositivo.getModelo())
                .fechaAsignacion(
                        asignacion.getFechaAsignacion()
                )
                .fechaRetiro(
                        asignacion.getFechaRetiro()
                )
                .estadoAsignacion(
                        asignacion.getEstadoAsignacion()
                )
                .observaciones(
                        asignacion.getObservaciones()
                )
                .build();
    }

    private void validarOrdenDisponibleParaAsignacion(
            OrdenTrabajo orden
    ) {
        if ("FINALIZADA".equals(orden.getEstado())
                || "CANCELADA".equals(orden.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden asignar dispositivos "
                            + "a una orden cerrada"
            );
        }
    }

    private void validarAccesoAOrden(
            OrdenTrabajo orden,
            String correoResponsable,
            boolean accesoGlobal
    ) {
        if (accesoGlobal) {
            return;
        }

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
                    "El técnico solo puede gestionar equipos de sus propias órdenes"
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

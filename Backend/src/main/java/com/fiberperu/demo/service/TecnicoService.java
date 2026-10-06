package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.tecnico.TecnicoRequest;
import com.fiberperu.demo.dto.tecnico.TecnicoResponse;
import com.fiberperu.demo.entity.RoleName;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioService usuarioService;

    /**
     * Lista todos los técnicos mediante DTO.
     */
    public List<TecnicoResponse> listarTodos() {
        return tecnicoRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Lista los técnicos activos y disponibles.
     */
    public List<TecnicoResponse> listarDisponibles() {
        return tecnicoRepository
                .findByEstadoTrueAndDisponibilidadTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un técnico por su identificador.
     */
    public TecnicoResponse buscarResponsePorId(
            Long idTecnico
    ) {
        Tecnico tecnico = buscarEntidadPorId(idTecnico);

        return convertirAResponse(tecnico);
    }

    /**
     * Busca un técnico por el identificador del usuario asociado.
     */
    public TecnicoResponse buscarResponsePorUsuario(
            Long idUsuario
    ) {
        Tecnico tecnico = tecnicoRepository
                .findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Técnico no encontrado para el usuario: "
                                        + idUsuario
                        )
                );

        return convertirAResponse(tecnico);
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public Tecnico buscarEntidadPorId(
            Long idTecnico
    ) {
        return tecnicoRepository.findById(idTecnico)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Técnico no encontrado con id: "
                                        + idTecnico
                        )
                );
    }

    /**
     * Registra un usuario con ROLE_TECNICO
     * y crea su perfil dentro de una misma transacción.
     */
    @Transactional
    public TecnicoResponse registrar(
            TecnicoRequest request
    ) {
        Usuario usuario = usuarioService.crearEntidadConRol(
                request.getUsuario(),
                RoleName.ROLE_TECNICO
        );

        Tecnico tecnico = Tecnico.builder()
                .usuario(usuario)
                .especialidad(
                        normalizarTextoOpcional(
                                request.getEspecialidad()
                        )
                )
                .disponibilidad(true)
                .estado(true)
                .build();

        Tecnico guardado =
                tecnicoRepository.save(tecnico);

        return convertirAResponse(guardado);
    }

    /**
     * Actualiza la especialidad del técnico.
     */
    @Transactional
    public TecnicoResponse actualizarEspecialidad(
            Long idTecnico,
            String especialidad
    ) {
        if (especialidad != null
                && especialidad.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "La especialidad no debe superar 100 caracteres"
            );
        }

        Tecnico tecnico =
                buscarEntidadPorId(idTecnico);

        tecnico.setEspecialidad(
                normalizarTextoOpcional(especialidad)
        );

        Tecnico actualizado =
                tecnicoRepository.save(tecnico);

        return convertirAResponse(actualizado);
    }

    /**
     * Modifica la disponibilidad del técnico.
     */
    @Transactional
    public TecnicoResponse cambiarDisponibilidad(
            Long idTecnico,
            boolean disponibilidad
    ) {
        Tecnico tecnico =
                buscarEntidadPorId(idTecnico);

        if (!Boolean.TRUE.equals(tecnico.getEstado())
                && disponibilidad) {
            throw new IllegalStateException(
                    "Un técnico inactivo no puede marcarse como disponible"
            );
        }

        tecnico.setDisponibilidad(disponibilidad);

        Tecnico actualizado =
                tecnicoRepository.save(tecnico);

        return convertirAResponse(actualizado);
    }

    /**
     * Activa o desactiva el perfil y la cuenta asociada.
     */
    @Transactional
    public TecnicoResponse cambiarEstado(
            Long idTecnico,
            boolean estado
    ) {
        Tecnico tecnico =
                buscarEntidadPorId(idTecnico);

        tecnico.setEstado(estado);
        tecnico.getUsuario().setEstado(estado);

        if (!estado) {
            tecnico.setDisponibilidad(false);
        }

        Tecnico actualizado =
                tecnicoRepository.save(tecnico);

        return convertirAResponse(actualizado);
    }

    /**
     * Convierte la entidad en un DTO seguro.
     */
    public TecnicoResponse convertirAResponse(
            Tecnico tecnico
    ) {
        Usuario usuario = tecnico.getUsuario();

        return TecnicoResponse.builder()
                .idTecnico(tecnico.getIdTecnico())
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .especialidad(tecnico.getEspecialidad())
                .disponibilidad(
                        tecnico.getDisponibilidad()
                )
                .estado(tecnico.getEstado())
                .build();
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
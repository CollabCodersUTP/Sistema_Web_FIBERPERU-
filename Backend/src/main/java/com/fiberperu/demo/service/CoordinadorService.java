package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.coordinador.CoordinadorRequest;
import com.fiberperu.demo.dto.coordinador.CoordinadorResponse;
import com.fiberperu.demo.entity.Coordinador;
import com.fiberperu.demo.entity.RoleName;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.CoordinadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinadorService {

    private final CoordinadorRepository coordinadorRepository;
    private final UsuarioService usuarioService;

    /**
     * Lista todos los coordinadores mediante DTO.
     */
    public List<CoordinadorResponse> listarTodos() {
        return coordinadorRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un coordinador por su identificador.
     */
    public CoordinadorResponse buscarResponsePorId(
            Long idCoordinador
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idCoordinador)
        );
    }

    /**
     * Busca un coordinador a partir del usuario asociado.
     */
    public CoordinadorResponse buscarResponsePorUsuario(
            Long idUsuario
    ) {
        Coordinador coordinador = coordinadorRepository
                .findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Coordinador no encontrado para el usuario: "
                                        + idUsuario
                        )
                );

        return convertirAResponse(coordinador);
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public Coordinador buscarEntidadPorId(
            Long idCoordinador
    ) {
        return coordinadorRepository.findById(idCoordinador)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Coordinador no encontrado con id: "
                                        + idCoordinador
                        )
                );
    }

    /**
     * Registra un usuario con ROLE_COORDINADOR
     * y crea su perfil dentro de una sola transacción.
     */
    @Transactional
    public CoordinadorResponse registrar(
            CoordinadorRequest request
    ) {
        Usuario usuario = usuarioService.crearEntidadConRol(
                request.getUsuario(),
                RoleName.ROLE_COORDINADOR
        );

        Coordinador coordinador = Coordinador.builder()
                .usuario(usuario)
                .cargo(request.getCargo().trim())
                .estado(true)
                .build();

        Coordinador guardado =
                coordinadorRepository.save(coordinador);

        return convertirAResponse(guardado);
    }

    /**
     * Actualiza el cargo del coordinador.
     */
    @Transactional
    public CoordinadorResponse actualizarCargo(
            Long idCoordinador,
            String cargo
    ) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException(
                    "El cargo es obligatorio"
            );
        }

        if (cargo.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El cargo no debe superar 100 caracteres"
            );
        }

        Coordinador coordinador =
                buscarEntidadPorId(idCoordinador);

        coordinador.setCargo(cargo.trim());

        Coordinador actualizado =
                coordinadorRepository.save(coordinador);

        return convertirAResponse(actualizado);
    }

    /**
     * Activa o desactiva el perfil y la cuenta asociada.
     */
    @Transactional
    public CoordinadorResponse cambiarEstado(
            Long idCoordinador,
            boolean estado
    ) {
        Coordinador coordinador =
                buscarEntidadPorId(idCoordinador);

        coordinador.setEstado(estado);
        coordinador.getUsuario().setEstado(estado);

        Coordinador actualizado =
                coordinadorRepository.save(coordinador);

        return convertirAResponse(actualizado);
    }

    /**
     * Convierte la entidad en una respuesta segura.
     */
    public CoordinadorResponse convertirAResponse(
            Coordinador coordinador
    ) {
        Usuario usuario = coordinador.getUsuario();

        return CoordinadorResponse.builder()
                .idCoordinador(
                        coordinador.getIdCoordinador()
                )
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .cargo(coordinador.getCargo())
                .estado(coordinador.getEstado())
                .build();
    }
}
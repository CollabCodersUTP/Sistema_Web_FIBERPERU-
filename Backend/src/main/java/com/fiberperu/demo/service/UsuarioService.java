package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.usuario.ActualizarUsuarioRequest;
import com.fiberperu.demo.dto.usuario.UsuarioRequest;
import com.fiberperu.demo.dto.usuario.UsuarioResponse;
import com.fiberperu.demo.entity.Rol;
import com.fiberperu.demo.entity.RoleName;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.RolRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios sin exponer el hash de la contraseña.
     */
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un usuario mediante su identificador.
     */
    public UsuarioResponse buscarResponsePorId(Long idUsuario) {
        return convertirAResponse(
                buscarEntidadPorId(idUsuario)
        );
    }

    /**
     * Busca un usuario mediante su correo.
     */
    public UsuarioResponse buscarResponsePorCorreo(String correo) {
        Usuario usuario = usuarioRepository
                .findByCorreo(normalizarCorreo(correo))
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario no encontrado con correo: "
                                        + correo
                        )
                );

        return convertirAResponse(usuario);
    }

    /**
     * Busca la entidad para operaciones internas.
     */
    public Usuario buscarEntidadPorId(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario no encontrado con id: "
                                        + idUsuario
                        )
                );
    }

    /**
     * Registra un usuario y transforma la contraseña a BCrypt.
     */
    @Transactional
    public UsuarioResponse registrar(
            UsuarioRequest request
    ) {
        Usuario usuario = crearEntidad(request);

        return convertirAResponse(usuario);
    }

    /**
     * Crea y persiste la entidad Usuario.
     *
     * Este método será reutilizado por ClienteService,
     * CoordinadorService y TecnicoService.
     */
    @Transactional
    public Usuario crearEntidad(
            UsuarioRequest request
    ) {
        validarCorreoDisponible(request.getCorreo());

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Rol no encontrado con id: "
                                        + request.getIdRol()
                        )
                );

        validarRolActivo(rol);

        Usuario usuario = Usuario.builder()
                .rol(rol)
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .correo(normalizarCorreo(request.getCorreo()))
                .contrasenaHash(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .telefono(
                        normalizarTextoOpcional(
                                request.getTelefono()
                        )
                )
                .estado(true)
                .build();

        return usuarioRepository.save(usuario);
    }

    /**
     * Crea un usuario y comprueba que su rol corresponda
     * al perfil que está siendo registrado.
     */
    @Transactional
    public Usuario crearEntidadConRol(
            UsuarioRequest request,
            RoleName rolEsperado
    ) {
        validarCorreoDisponible(request.getCorreo());

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Rol no encontrado con id: "
                                        + request.getIdRol()
                        )
                );

        validarRolActivo(rol);

        if (rol.getNombre() != rolEsperado) {
            throw new IllegalArgumentException(
                    "El rol seleccionado debe ser "
                            + rolEsperado.name()
            );
        }

        Usuario usuario = Usuario.builder()
                .rol(rol)
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .correo(normalizarCorreo(request.getCorreo()))
                .contrasenaHash(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .telefono(
                        normalizarTextoOpcional(
                                request.getTelefono()
                        )
                )
                .estado(true)
                .build();

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambia el estado activo o inactivo de un usuario.
     */
    @Transactional
    public UsuarioResponse cambiarEstado(
            Long idUsuario,
            boolean estado
    ) {
        Usuario usuario = buscarEntidadPorId(idUsuario);
        usuario.setEstado(estado);

        Usuario actualizado =
                usuarioRepository.save(usuario);

        return convertirAResponse(actualizado);
    }

    @Transactional
    public UsuarioResponse actualizar(
            Long idUsuario,
            ActualizarUsuarioRequest request
    ) {
        Usuario usuario = buscarEntidadPorId(idUsuario);
        String correo = normalizarCorreo(request.getCorreo());
        usuarioRepository.findByCorreo(correo)
                .filter(encontrado -> !encontrado.getIdUsuario().equals(idUsuario))
                .ifPresent(encontrado -> {
                    throw new IllegalArgumentException("El correo ya está registrado");
                });

        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(normalizarTextoOpcional(request.getTelefono()));
        return convertirAResponse(usuarioRepository.save(usuario));
    }

    /**
     * Convierte la entidad a una respuesta segura.
     * No incluye contrasenaHash.
     */
    public UsuarioResponse convertirAResponse(
            Usuario usuario
    ) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .idRol(usuario.getRol().getIdRol())
                .rol(usuario.getRol().getNombre().name())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .estado(usuario.getEstado())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    private void validarCorreoDisponible(String correo) {
        String correoNormalizado =
                normalizarCorreo(correo);

        if (usuarioRepository.existsByCorreo(
                correoNormalizado
        )) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }
    }

    private void validarRolActivo(Rol rol) {
        if (!Boolean.TRUE.equals(rol.getEstado())) {
            throw new IllegalStateException(
                    "El rol seleccionado se encuentra inactivo"
            );
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}

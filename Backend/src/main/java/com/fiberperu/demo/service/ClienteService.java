package com.fiberperu.demo.service;

import com.fiberperu.demo.dto.cliente.ActualizarClienteRequest;
import com.fiberperu.demo.dto.cliente.ClienteRequest;
import com.fiberperu.demo.dto.cliente.ClienteResponse;
import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.RoleName;
import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private static final Set<String> TIPOS_DOCUMENTO_PERMITIDOS =
            Set.of(
                    "DNI",
                    "RUC",
                    "CE",
                    "PASAPORTE"
            );

    private final ClienteRepository clienteRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    /**
     * Lista todos los clientes mediante DTO.
     */
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Busca un cliente por su identificador.
     */
    public ClienteResponse buscarResponsePorId(
            Long idCliente
    ) {
        return convertirAResponse(
                buscarEntidadPorId(idCliente)
        );
    }

    /**
     * Busca un cliente por su número de documento.
     */
    public ClienteResponse buscarResponsePorDocumento(
            String numeroDocumento
    ) {
        Cliente cliente = clienteRepository
                .findByNumeroDocumento(
                        numeroDocumento.trim()
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado con documento: "
                                        + numeroDocumento
                        )
                );

        return convertirAResponse(cliente);
    }

    /**
     * Busca la entidad Cliente para operaciones internas.
     */
    public Cliente buscarEntidadPorId(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cliente no encontrado con id: "
                                        + idCliente
                        )
                );
    }

    /**
     * Registra el usuario y el perfil de cliente
     * dentro de una sola transacción.
     */
    @Transactional
    public ClienteResponse registrar(
            ClienteRequest request
    ) {
        String numeroDocumento =
                request.getNumeroDocumento().trim();

        if (clienteRepository.existsByNumeroDocumento(
                numeroDocumento
        )) {
            throw new IllegalArgumentException(
                    "El número de documento ya está registrado"
            );
        }

        String tipoDocumento = request.getTipoDocumento()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!TIPOS_DOCUMENTO_PERMITIDOS.contains(
                tipoDocumento
        )) {
            throw new IllegalArgumentException(
                    "Tipo de documento no permitido: "
                            + tipoDocumento
            );
        }

        validarLongitudDocumento(
                tipoDocumento,
                numeroDocumento
        );

        Usuario usuario = usuarioService.crearEntidadConRol(
                request.getUsuario(),
                RoleName.ROLE_CLIENTE
        );

        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .razonSocial(
                        normalizarTextoOpcional(
                                request.getRazonSocial()
                        )
                )
                .direccion(
                        request.getDireccion().trim()
                )
                .estado(true)
                .build();

        Cliente guardado =
                clienteRepository.save(cliente);

        return convertirAResponse(guardado);
    }

    /**
     * Cambia el estado del cliente y de su cuenta.
     */
    @Transactional
    public ClienteResponse cambiarEstado(
            Long idCliente,
            boolean estado
    ) {
        Cliente cliente =
                buscarEntidadPorId(idCliente);

        cliente.setEstado(estado);
        cliente.getUsuario().setEstado(estado);

        Cliente actualizado =
                clienteRepository.save(cliente);

        return convertirAResponse(actualizado);
    }

    @Transactional
    public ClienteResponse actualizar(
            Long idCliente,
            ActualizarClienteRequest request
    ) {
        Cliente cliente = buscarEntidadPorId(idCliente);
        String numeroDocumento = request.getNumeroDocumento().trim();
        clienteRepository.findByNumeroDocumento(numeroDocumento)
                .filter(encontrado -> !encontrado.getIdCliente().equals(idCliente))
                .ifPresent(encontrado -> {
                    throw new IllegalArgumentException("El número de documento ya está registrado");
                });

        String tipoDocumento = request.getTipoDocumento().trim().toUpperCase(Locale.ROOT);
        if (!TIPOS_DOCUMENTO_PERMITIDOS.contains(tipoDocumento)) {
            throw new IllegalArgumentException("Tipo de documento no permitido: " + tipoDocumento);
        }
        validarLongitudDocumento(tipoDocumento, numeroDocumento);

        Usuario usuario = cliente.getUsuario();
        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);
        usuarioRepository.findByCorreo(correo)
                .filter(encontrado -> !encontrado.getIdUsuario().equals(usuario.getIdUsuario()))
                .ifPresent(encontrado -> {
                    throw new IllegalArgumentException("El correo ya está registrado");
                });
        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(normalizarTextoOpcional(request.getTelefono()));
        cliente.setTipoDocumento(tipoDocumento);
        cliente.setNumeroDocumento(numeroDocumento);
        cliente.setRazonSocial(normalizarTextoOpcional(request.getRazonSocial()));
        cliente.setDireccion(request.getDireccion().trim());
        return convertirAResponse(clienteRepository.save(cliente));
    }

    /**
     * Convierte la entidad a una respuesta segura.
     */
    public ClienteResponse convertirAResponse(
            Cliente cliente
    ) {
        Usuario usuario = cliente.getUsuario();

        return ClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .tipoDocumento(
                        cliente.getTipoDocumento()
                )
                .numeroDocumento(
                        cliente.getNumeroDocumento()
                )
                .razonSocial(cliente.getRazonSocial())
                .direccion(cliente.getDireccion())
                .estado(cliente.getEstado())
                .build();
    }

    /**
     * Aplica validaciones mínimas para DNI y RUC.
     */
    private void validarLongitudDocumento(
            String tipoDocumento,
            String numeroDocumento
    ) {
        if ("DNI".equals(tipoDocumento)
                && !numeroDocumento.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos"
            );
        }

        if ("RUC".equals(tipoDocumento)
                && !numeroDocumento.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                    "El RUC debe contener exactamente 11 dígitos"
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

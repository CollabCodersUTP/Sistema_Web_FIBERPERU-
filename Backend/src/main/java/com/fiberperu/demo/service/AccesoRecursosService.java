package com.fiberperu.demo.service;

import com.fiberperu.demo.entity.Cliente;
import com.fiberperu.demo.entity.OrdenTrabajo;
import com.fiberperu.demo.entity.Tecnico;
import com.fiberperu.demo.exception.RecursoNoEncontradoException;
import com.fiberperu.demo.repository.ClienteRepository;
import com.fiberperu.demo.repository.OrdenTrabajoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccesoRecursosService {

    private final ClienteRepository clienteRepository;
    private final OrdenTrabajoRepository ordenRepository;

    public void verificarAccesoCliente(Long idCliente, Authentication authentication) {
        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "COORDINADOR")) {
            return;
        }

        if (!tieneRol(authentication, "CLIENTE")) {
            throw accesoDenegado();
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con id: " + idCliente));
        if (!Objects.equals(cliente.getUsuario().getCorreo(), authentication.getName())) {
            throw accesoDenegado();
        }
    }

    public void verificarAccesoOrden(Long idOrden, Authentication authentication) {
        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "COORDINADOR")) {
            return;
        }

        OrdenTrabajo orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden no encontrada con id: " + idOrden));

        if (tieneRol(authentication, "TECNICO")) {
            Tecnico tecnico = orden.getTecnico();
            if (tecnico != null && Objects.equals(
                    tecnico.getUsuario().getCorreo(), authentication.getName())) {
                return;
            }
        }

        if (tieneRol(authentication, "CLIENTE") && Objects.equals(
                orden.getSolicitud().getCliente().getUsuario().getCorreo(),
                authentication.getName())) {
            return;
        }

        throw accesoDenegado();
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + rol));
    }

    private AccessDeniedException accesoDenegado() {
        return new AccessDeniedException("No tiene acceso a este recurso");
    }
}

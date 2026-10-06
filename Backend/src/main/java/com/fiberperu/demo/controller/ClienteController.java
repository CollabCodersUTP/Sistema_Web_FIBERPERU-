package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.cliente.ActualizarClienteRequest;
import com.fiberperu.demo.dto.cliente.ClienteRequest;
import com.fiberperu.demo.dto.cliente.ClienteResponse;
import com.fiberperu.demo.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Lista todos los clientes.
     * Disponible para administradores y coordinadores.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(
                clienteService.listarTodos()
        );
    }

    /**
     * Busca un cliente mediante su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                clienteService.buscarResponsePorId(id)
        );
    }

    /**
     * Busca un cliente mediante su número de documento.
     */
    @GetMapping("/documento/{numeroDocumento}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<ClienteResponse> buscarPorDocumento(
            @PathVariable String numeroDocumento
    ) {
        return ResponseEntity.ok(
                clienteService.buscarResponsePorDocumento(
                        numeroDocumento
                )
        );
    }

    /**
     * Registra la cuenta de usuario y el perfil del cliente.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ClienteResponse> registrar(
            @Valid
            @RequestBody ClienteRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        clienteService.registrar(request)
                );
    }

    /**
     * Activa o desactiva el perfil y la cuenta del cliente.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ClienteResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                clienteService.cambiarEstado(id, activo)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteRequest request
    ) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }
}

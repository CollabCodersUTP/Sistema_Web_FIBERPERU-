package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.usuario.ActualizarUsuarioRequest;
import com.fiberperu.demo.dto.usuario.UsuarioRequest;
import com.fiberperu.demo.dto.usuario.UsuarioResponse;
import com.fiberperu.demo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Lista todos los usuarios.
     * Solo puede acceder el administrador.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(
                usuarioService.listarTodos()
        );
    }

    /**
     * Busca un usuario mediante su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                usuarioService.buscarResponsePorId(id)
        );
    }

    /**
     * Busca un usuario mediante el correo.
     */
    @GetMapping("/correo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> buscarPorCorreo(
            @RequestParam String valor
    ) {
        return ResponseEntity.ok(
                usuarioService.buscarResponsePorCorreo(valor)
        );
    }

    /**
     * Registra una cuenta de usuario.
     *
     * Los perfiles Cliente, Coordinador y Técnico
     * se registrarán mediante sus propios endpoints.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid
            @RequestBody UsuarioRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        usuarioService.registrar(request)
                );
    }

    /**
     * Activa o desactiva una cuenta.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                usuarioService.cambiarEstado(id, activo)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }
}

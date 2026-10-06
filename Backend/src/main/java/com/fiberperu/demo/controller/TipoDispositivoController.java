package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.dispositivo.TipoDispositivoRequest;
import com.fiberperu.demo.dto.dispositivo.TipoDispositivoResponse;
import com.fiberperu.demo.service.TipoDispositivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-dispositivo")
@RequiredArgsConstructor
public class TipoDispositivoController {

    private final TipoDispositivoService tipoDispositivoService;

    /**
     * Lista todos los tipos de dispositivo.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<TipoDispositivoResponse>>
    listarTodos() {
        return ResponseEntity.ok(
                tipoDispositivoService.listarTodos()
        );
    }

    /**
     * Busca un tipo mediante su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<TipoDispositivoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                tipoDispositivoService.buscarResponsePorId(id)
        );
    }

    /**
     * Busca un tipo mediante su nombre.
     */
    @GetMapping("/nombre/{nombre}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<TipoDispositivoResponse>
    buscarPorNombre(
            @PathVariable String nombre
    ) {
        return ResponseEntity.ok(
                tipoDispositivoService
                        .buscarResponsePorNombre(nombre)
        );
    }

    /**
     * Registra un nuevo tipo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoDispositivoResponse> registrar(
            @Valid
            @RequestBody TipoDispositivoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        tipoDispositivoService.registrar(request)
                );
    }

    /**
     * Actualiza un tipo existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoDispositivoResponse> actualizar(
            @PathVariable Long id,
            @Valid
            @RequestBody TipoDispositivoRequest request
    ) {
        return ResponseEntity.ok(
                tipoDispositivoService.actualizar(
                        id,
                        request
                )
        );
    }

    /**
     * Activa o desactiva un tipo.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TipoDispositivoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                tipoDispositivoService.cambiarEstado(
                        id,
                        activo
                )
        );
    }
}
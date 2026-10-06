package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.tecnico.ActualizarEspecialidadRequest;
import com.fiberperu.demo.dto.tecnico.TecnicoRequest;
import com.fiberperu.demo.dto.tecnico.TecnicoResponse;
import com.fiberperu.demo.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    /**
     * Lista todos los técnicos.
     * Disponible para administradores y coordinadores.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<TecnicoResponse>> listarTodos() {
        return ResponseEntity.ok(
                tecnicoService.listarTodos()
        );
    }

    /**
     * Lista únicamente los técnicos activos y disponibles.
     */
    @GetMapping("/disponibles")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<TecnicoResponse>> listarDisponibles() {
        return ResponseEntity.ok(
                tecnicoService.listarDisponibles()
        );
    }

    /**
     * Busca un técnico mediante su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<TecnicoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                tecnicoService.buscarResponsePorId(id)
        );
    }

    /**
     * Busca un técnico mediante el usuario asociado.
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<TecnicoResponse> buscarPorUsuario(
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(
                tecnicoService.buscarResponsePorUsuario(
                        idUsuario
                )
        );
    }

    /**
     * Registra la cuenta de usuario y el perfil técnico
     * dentro de una misma transacción.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TecnicoResponse> registrar(
            @Valid
            @RequestBody TecnicoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        tecnicoService.registrar(request)
                );
    }

    /**
     * Actualiza la especialidad del técnico.
     */
    @PatchMapping("/{id}/especialidad")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TecnicoResponse> actualizarEspecialidad(
            @PathVariable Long id,
            @Valid
            @RequestBody ActualizarEspecialidadRequest request
    ) {
        return ResponseEntity.ok(
                tecnicoService.actualizarEspecialidad(
                        id,
                        request.getEspecialidad()
                )
        );
    }

    /**
     * Cambia la disponibilidad del técnico.
     */
    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<TecnicoResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible
    ) {
        return ResponseEntity.ok(
                tecnicoService.cambiarDisponibilidad(
                        id,
                        disponible
                )
        );
    }

    /**
     * Activa o desactiva el perfil técnico
     * y la cuenta de usuario asociada.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TecnicoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                tecnicoService.cambiarEstado(
                        id,
                        activo
                )
        );
    }
}
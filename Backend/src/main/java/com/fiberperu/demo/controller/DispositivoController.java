package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.dispositivo.DispositivoRequest;
import com.fiberperu.demo.dto.dispositivo.DispositivoResponse;
import com.fiberperu.demo.service.DispositivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {

    private final DispositivoService dispositivoService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<DispositivoResponse>> listarTodos() {
        return ResponseEntity.ok(
                dispositivoService.listarTodos()
        );
    }

    @GetMapping("/disponibles")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<DispositivoResponse>> listarDisponibles() {
        return ResponseEntity.ok(
                dispositivoService.listarDisponibles()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<DispositivoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                dispositivoService.buscarResponsePorId(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DispositivoResponse> registrar(
            @Valid @RequestBody DispositivoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dispositivoService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DispositivoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DispositivoRequest request
    ) {
        return ResponseEntity.ok(dispositivoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<DispositivoResponse> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible
    ) {
        return ResponseEntity.ok(
                dispositivoService.cambiarDisponibilidad(
                        id,
                        disponible
                )
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DispositivoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado
    ) {
        return ResponseEntity.ok(
                dispositivoService.cambiarEstado(id, estado)
        );
    }
}

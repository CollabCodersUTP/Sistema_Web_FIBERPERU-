package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.orden.*;
import com.fiberperu.demo.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenTrabajoService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<OrdenTrabajoResponse>>
    listarTodas() {

        return ResponseEntity.ok(
                ordenTrabajoService.listarTodas()
        );
    }

    @GetMapping("/mis-ordenes")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoResponse>>
    listarMisOrdenes(Authentication authentication) {
        return ResponseEntity.ok(
                ordenTrabajoService.listarMisOrdenes(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrdenTrabajoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordenTrabajoService.buscarResponsePorId(id)
        );
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrdenTrabajoResponse> buscarPorCodigo(
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(
                ordenTrabajoService.buscarResponsePorCodigo(codigo)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<OrdenTrabajoResponse> crear(
            @Valid
            @RequestBody CrearOrdenTrabajoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ordenTrabajoService.crear(
                                request,
                                authentication.getName()
                        )
                );
    }

    @PatchMapping("/{id}/tecnico")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<OrdenTrabajoResponse> asignarTecnico(
            @PathVariable Long id,
            @Valid
            @RequestBody AsignarTecnicoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ordenTrabajoService.asignarTecnico(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/programacion")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<OrdenTrabajoResponse> programar(
            @PathVariable Long id,
            @Valid
            @RequestBody ProgramarOrdenRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ordenTrabajoService.programar(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<OrdenTrabajoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid
            @RequestBody CambiarEstadoOrdenRequest request,
            Authentication authentication
    ) {
        boolean puedeCerrar = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                                || authority.getAuthority().equals("ROLE_COORDINADOR")
                );
        return ResponseEntity.ok(
                ordenTrabajoService.cambiarEstado(
                        id,
                        request,
                        authentication.getName(),
                        puedeCerrar
                )
        );
    }
}

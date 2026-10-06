package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.asignacion.OrdenDispositivoRequest;
import com.fiberperu.demo.dto.asignacion.OrdenDispositivoResponse;
import com.fiberperu.demo.dto.asignacion.RetirarDispositivoRequest;
import com.fiberperu.demo.service.OrdenDispositivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes-dispositivos")
@RequiredArgsConstructor
public class OrdenDispositivoController {

    private final OrdenDispositivoService ordenDispositivoService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<OrdenDispositivoResponse>>
    listarTodas() {
        return ResponseEntity.ok(
                ordenDispositivoService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<OrdenDispositivoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordenDispositivoService.buscarResponsePorId(id)
        );
    }

    @GetMapping("/orden/{idOrden}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<OrdenDispositivoResponse>>
    listarPorOrden(
            @PathVariable Long idOrden
    ) {
        return ResponseEntity.ok(
                ordenDispositivoService.listarPorOrden(idOrden)
        );
    }

    @GetMapping("/dispositivo/{idDispositivo}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<OrdenDispositivoResponse>>
    listarPorDispositivo(
            @PathVariable Long idDispositivo
    ) {
        return ResponseEntity.ok(
                ordenDispositivoService
                        .listarPorDispositivo(idDispositivo)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<OrdenDispositivoResponse> asignar(
            @Valid @RequestBody OrdenDispositivoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ordenDispositivoService.asignar(
                                request,
                                authentication.getName(),
                                tieneAccesoGlobal(authentication)
                        )
                );
    }

    @PatchMapping("/{id}/instalar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<OrdenDispositivoResponse>
    marcarComoInstalado(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ordenDispositivoService
                        .marcarComoInstalado(
                                id,
                                authentication.getName(),
                                tieneAccesoGlobal(authentication)
                        )
        );
    }

    @PatchMapping("/{id}/retirar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<OrdenDispositivoResponse> retirar(
            @PathVariable Long id,
            @Valid @RequestBody RetirarDispositivoRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                        ordenDispositivoService.retirar(
                                id,
                                request,
                                authentication.getName(),
                                tieneAccesoGlobal(authentication)
                        )
                );
    }

    private boolean tieneAccesoGlobal(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || authority.getAuthority().equals("ROLE_COORDINADOR")
        );
    }
}

package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.solicitud.EvaluarSolicitudRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionRequest;
import com.fiberperu.demo.dto.solicitud.SolicitudInstalacionResponse;
import com.fiberperu.demo.service.SolicitudInstalacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudInstalacionController {

    private final SolicitudInstalacionService solicitudService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<SolicitudInstalacionResponse>>
    listarTodas() {

        return ResponseEntity.ok(
                solicitudService.listarTodas()
        );
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<SolicitudInstalacionResponse>>
    listarMisSolicitudes(Authentication authentication) {
        return ResponseEntity.ok(
                solicitudService.listarMisSolicitudes(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudInstalacionResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitudService.buscarResponsePorId(id)
        );
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudInstalacionResponse>
    buscarPorCodigo(
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(
                solicitudService.buscarResponsePorCodigo(codigo)
        );
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SolicitudInstalacionResponse>>
    listarPorCliente(
            @PathVariable Long idCliente
    ) {
        return ResponseEntity.ok(
                solicitudService.listarPorCliente(idCliente)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'ADMINISTRADOR')"
    )
    public ResponseEntity<SolicitudInstalacionResponse>
    registrar(
            @Valid
            @RequestBody SolicitudInstalacionRequest request,
            Authentication authentication
    ) {
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_CLIENTE")
                );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        solicitudService.registrar(
                                request,
                                authentication.getName(),
                                esCliente
                        )
                );
    }

    @PatchMapping("/{id}/evaluar")
    @PreAuthorize(
            "hasAnyRole('COORDINADOR', 'ADMINISTRADOR')"
    )
    public ResponseEntity<SolicitudInstalacionResponse>
    evaluar(
            @PathVariable Long id,
            @Valid
            @RequestBody EvaluarSolicitudRequest request
    ) {
        return ResponseEntity.ok(
                solicitudService.evaluar(id, request)
        );
    }
}

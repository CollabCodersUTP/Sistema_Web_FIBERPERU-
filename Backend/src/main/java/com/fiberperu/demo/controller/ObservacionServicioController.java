package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.observacion.ActualizarObservacionRequest;
import com.fiberperu.demo.dto.observacion.ObservacionServicioRequest;
import com.fiberperu.demo.dto.observacion.ObservacionServicioResponse;
import com.fiberperu.demo.service.ObservacionServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/observaciones")
@RequiredArgsConstructor
public class ObservacionServicioController {

    private final ObservacionServicioService observacionService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<ObservacionServicioResponse>>
    listarTodas() {
        return ResponseEntity.ok(
                observacionService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<ObservacionServicioResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                observacionService.buscarResponsePorId(id)
        );
    }

    @GetMapping("/orden/{idOrden}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<ObservacionServicioResponse>>
    listarPorOrden(
            @PathVariable Long idOrden
    ) {
        return ResponseEntity.ok(
                observacionService.listarPorOrden(idOrden)
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'TECNICO')"
    )
    public ResponseEntity<ObservacionServicioResponse>
    registrar(
            @Valid
            @RequestBody ObservacionServicioRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        observacionService.registrar(request)
                );
    }

    @PatchMapping("/{id}/descripcion")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'TECNICO')"
    )
    public ResponseEntity<ObservacionServicioResponse>
    actualizarDescripcion(
            @PathVariable Long id,
            @Valid
            @RequestBody ActualizarObservacionRequest request
    ) {
        return ResponseEntity.ok(
                observacionService.actualizarDescripcion(
                        id,
                        request.getDescripcion()
                )
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ObservacionServicioResponse>
    cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                observacionService.cambiarEstado(
                        id,
                        activo
                )
        );
    }
}
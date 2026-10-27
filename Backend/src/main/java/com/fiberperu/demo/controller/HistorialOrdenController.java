package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.historial.HistorialOrdenResponse;
import com.fiberperu.demo.service.AccesoRecursosService;
import com.fiberperu.demo.service.HistorialOrdenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historial-ordenes")
@RequiredArgsConstructor
public class HistorialOrdenController {

    private final HistorialOrdenService historialOrdenService;
    private final AccesoRecursosService accesoRecursosService;

    /**
     * Lista todos los eventos históricos.
     * Solo está disponible para el administrador.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<HistorialOrdenResponse>>
    listarTodos() {
        return ResponseEntity.ok(
                historialOrdenService.listarTodos()
        );
    }

    /**
     * Busca un evento histórico por su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<HistorialOrdenResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                historialOrdenService.buscarResponsePorId(id)
        );
    }

    /**
     * Devuelve el historial cronológico de una orden.
     */
    @GetMapping("/orden/{idOrden}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<HistorialOrdenResponse>>
    listarPorOrden(
            @PathVariable Long idOrden,
            Authentication authentication
    ) {
        accesoRecursosService.verificarAccesoOrden(idOrden, authentication);
        return ResponseEntity.ok(
                historialOrdenService.listarPorOrden(idOrden)
        );
    }
}

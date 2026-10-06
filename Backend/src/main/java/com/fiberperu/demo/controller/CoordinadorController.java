package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.coordinador.ActualizarCargoRequest;
import com.fiberperu.demo.dto.coordinador.CoordinadorRequest;
import com.fiberperu.demo.dto.coordinador.CoordinadorResponse;
import com.fiberperu.demo.service.CoordinadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coordinadores")
@RequiredArgsConstructor
public class CoordinadorController {

    private final CoordinadorService coordinadorService;

    /**
     * Lista todos los coordinadores.
     * Solo puede acceder el administrador.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CoordinadorResponse>> listarTodos() {
        return ResponseEntity.ok(
                coordinadorService.listarTodos()
        );
    }

    /**
     * Busca un coordinador mediante su identificador.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<CoordinadorResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                coordinadorService.buscarResponsePorId(id)
        );
    }

    /**
     * Busca un coordinador mediante el usuario asociado.
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CoordinadorResponse> buscarPorUsuario(
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(
                coordinadorService.buscarResponsePorUsuario(
                        idUsuario
                )
        );
    }

    /**
     * Registra la cuenta de usuario y el perfil
     * del coordinador en una misma transacción.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CoordinadorResponse> registrar(
            @Valid
            @RequestBody CoordinadorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        coordinadorService.registrar(request)
                );
    }

    /**
     * Actualiza el cargo del coordinador.
     */
    @PatchMapping("/{id}/cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CoordinadorResponse> actualizarCargo(
            @PathVariable Long id,
            @Valid
            @RequestBody ActualizarCargoRequest request
    ) {
        return ResponseEntity.ok(
                coordinadorService.actualizarCargo(
                        id,
                        request.getCargo()
                )
        );
    }

    /**
     * Activa o desactiva el perfil del coordinador
     * y su cuenta de usuario asociada.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CoordinadorResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        return ResponseEntity.ok(
                coordinadorService.cambiarEstado(
                        id,
                        activo
                )
        );
    }
}
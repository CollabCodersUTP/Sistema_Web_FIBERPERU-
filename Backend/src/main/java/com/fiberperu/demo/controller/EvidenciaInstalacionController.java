package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionRequest;
import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionResponse;
import com.fiberperu.demo.dto.evidencia.ValidarEvidenciaRequest;
import com.fiberperu.demo.service.EvidenciaInstalacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evidencias")
@RequiredArgsConstructor
public class EvidenciaInstalacionController {

    private final EvidenciaInstalacionService evidenciaService;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<EvidenciaInstalacionResponse>>
    listarTodas() {
        return ResponseEntity.ok(
                evidenciaService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<EvidenciaInstalacionResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                evidenciaService.buscarResponsePorId(id)
        );
    }

    @PostMapping(value = "/archivo", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<EvidenciaInstalacionResponse> registrarArchivo(
            @RequestParam Long idOrden,
            @RequestParam MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            Authentication authentication
    ) {
        boolean esAdministrador = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                evidenciaService.registrarArchivo(
                        idOrden,
                        archivo,
                        descripcion,
                        authentication.getName(),
                        esAdministrador
                )
        );
    }

    @GetMapping("/orden/{idOrden}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<EvidenciaInstalacionResponse>>
    listarPorOrden(
            @PathVariable Long idOrden
    ) {
        return ResponseEntity.ok(
                evidenciaService.listarPorOrden(idOrden)
        );
    }

    @GetMapping("/pendientes")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR')"
    )
    public ResponseEntity<List<EvidenciaInstalacionResponse>>
    listarPendientes() {
        return ResponseEntity.ok(
                evidenciaService.listarPendientes()
        );
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'TECNICO')"
    )
    public ResponseEntity<EvidenciaInstalacionResponse>
    registrar(
            @Valid
            @RequestBody EvidenciaInstalacionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        evidenciaService.registrar(request)
                );
    }

    @PatchMapping("/{id}/validacion")
    @PreAuthorize("hasRole('COORDINADOR')")
    public ResponseEntity<EvidenciaInstalacionResponse>
    validar(
            @PathVariable Long id,
            @Valid
            @RequestBody ValidarEvidenciaRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                evidenciaService.validar(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }
}

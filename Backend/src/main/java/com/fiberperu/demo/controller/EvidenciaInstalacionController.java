package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionRequest;
import com.fiberperu.demo.dto.evidencia.EvidenciaInstalacionResponse;
import com.fiberperu.demo.dto.evidencia.ValidarEvidenciaRequest;
import com.fiberperu.demo.service.AccesoRecursosService;
import com.fiberperu.demo.service.EvidenciaInstalacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/evidencias")
@RequiredArgsConstructor
public class EvidenciaInstalacionController {

    private final EvidenciaInstalacionService evidenciaService;
    private final AccesoRecursosService accesoRecursosService;

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
            @PathVariable Long id,
            Authentication authentication
    ) {
        EvidenciaInstalacionResponse evidencia = evidenciaService.buscarResponsePorId(id);
        accesoRecursosService.verificarAccesoOrden(evidencia.getIdOrden(), authentication);
        return ResponseEntity.ok(evidencia);
    }

    @GetMapping("/{id}/archivo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')")
    public ResponseEntity<byte[]> verArchivo(
            @PathVariable Long id, Authentication authentication
    ) {
        boolean accesoGeneral = authentication.getAuthorities().stream()
                .anyMatch(authority -> Set.of(
                        "ROLE_ADMINISTRADOR", "ROLE_COORDINADOR"
                ).contains(authority.getAuthority()));
        var archivo = evidenciaService.obtenerArchivo(
                id, authentication.getName(), accesoGeneral
        );
        String tipoGuardado = archivo.tipo() == null ? ""
                : archivo.tipo().trim().toLowerCase(Locale.ROOT);
        String tipo = Set.of("image/jpeg", "image/png", "application/pdf")
                .contains(tipoGuardado)
                ? tipoGuardado : "application/octet-stream";

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(tipo))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(archivo.contenido());
    }

    @PostMapping(value = "/archivo", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<EvidenciaInstalacionResponse> registrarArchivo(
            @RequestParam Long idOrden,
            @RequestParam MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            Authentication authentication
    ) {
        accesoRecursosService.verificarAccesoOrden(idOrden, authentication);
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

    @PostMapping(value = "/{id}/reenviar-archivo", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<EvidenciaInstalacionResponse> reenviarArchivo(
            @PathVariable Long id,
            @RequestParam MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            Authentication authentication
    ) {
        EvidenciaInstalacionResponse evidencia = evidenciaService.buscarResponsePorId(id);
        accesoRecursosService.verificarAccesoOrden(evidencia.getIdOrden(), authentication);
        boolean esAdministrador = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        return ResponseEntity.ok(evidenciaService.reenviarArchivo(
                id, archivo, descripcion, authentication.getName(), esAdministrador
        ));
    }

    @GetMapping("/orden/{idOrden}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')"
    )
    public ResponseEntity<List<EvidenciaInstalacionResponse>>
    listarPorOrden(
            @PathVariable Long idOrden,
            Authentication authentication
    ) {
        accesoRecursosService.verificarAccesoOrden(idOrden, authentication);
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
            @RequestBody EvidenciaInstalacionRequest request,
            Authentication authentication
    ) {
        accesoRecursosService.verificarAccesoOrden(request.getIdOrden(), authentication);
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

package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.notificacion.NotificacionTecnicoResponse;
import com.fiberperu.demo.service.NotificacionTecnicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECNICO')")
public class NotificacionTecnicoController {

    private final NotificacionTecnicoService notificacionService;

    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionTecnicoResponse>> listarMias(
            Authentication authentication) {
        return ResponseEntity.ok(notificacionService.listarMias(authentication.getName()));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionTecnicoResponse> marcarLeida(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(
                notificacionService.marcarLeida(id, authentication.getName()));
    }
}

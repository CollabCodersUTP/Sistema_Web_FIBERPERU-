package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.historial.HistorialSolicitudResponse;
import com.fiberperu.demo.service.HistorialSolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historial-solicitudes")
@RequiredArgsConstructor
public class HistorialSolicitudController {
    private final HistorialSolicitudService historialSolicitudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<HistorialSolicitudResponse>> listarTodos() {
        return ResponseEntity.ok(historialSolicitudService.listarTodos());
    }
}

package com.fiberperu.demo.controller;

import com.fiberperu.demo.dto.seguimiento.SeguimientoPublicoResponse;
import com.fiberperu.demo.service.SeguimientoPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/seguimiento")
@RequiredArgsConstructor
public class SeguimientoPublicoController {

    private final SeguimientoPublicoService seguimientoService;

    @GetMapping("/{codigo}")
    public ResponseEntity<SeguimientoPublicoResponse> consultar(
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(seguimientoService.consultar(codigo));
    }
}

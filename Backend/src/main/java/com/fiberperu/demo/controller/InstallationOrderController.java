package com.fiberperu.demo.controller;

import com.fiberperu.demo.entity.InstallationOrder;
import com.fiberperu.demo.repository.InstallationOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class InstallationOrderController {

    private final InstallationOrderRepository orderRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<List<InstallationOrder>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO', 'CLIENTE')")
    public ResponseEntity<InstallationOrder> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tecnico/{tecnicoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')")
    public ResponseEntity<List<InstallationOrder>> getOrdersByTechnician(@PathVariable Long tecnicoId) {
        return ResponseEntity.ok(orderRepository.findByTecnicoId(tecnicoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<InstallationOrder> createOrder(@RequestBody InstallationOrder order) {
        InstallationOrder savedOrder = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')")
    public ResponseEntity<InstallationOrder> updateOrderStatus(@PathVariable Long id, @RequestParam String estado) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setEstado(estado);
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

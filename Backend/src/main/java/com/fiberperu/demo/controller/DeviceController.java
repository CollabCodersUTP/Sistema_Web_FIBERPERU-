package com.fiberperu.demo.controller;

import com.fiberperu.demo.entity.Device;
import com.fiberperu.demo.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dispositivos")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')")
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR', 'TECNICO')")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return deviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device savedDevice = deviceRepository.save(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDevice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        return deviceRepository.findById(id)
                .map(device -> {
                    device.setNumeroSerie(deviceDetails.getNumeroSerie());
                    device.setTipo(deviceDetails.getTipo());
                    device.setMarca(deviceDetails.getMarca());
                    device.setModelo(deviceDetails.getModelo());
                    device.setMacAddress(deviceDetails.getMacAddress());
                    device.setEstado(deviceDetails.getEstado());
                    return ResponseEntity.ok(deviceRepository.save(device));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

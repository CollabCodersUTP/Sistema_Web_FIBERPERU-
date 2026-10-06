package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispositivoRepository
        extends JpaRepository<Dispositivo, Long> {

    Optional<Dispositivo> findByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerie(String numeroSerie);

    List<Dispositivo> findByDisponibilidadTrue();
}
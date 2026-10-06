package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.TipoDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoDispositivoRepository
        extends JpaRepository<TipoDispositivo, Long> {

    Optional<TipoDispositivo> findByNombre(String nombre);
}
package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Coordinador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoordinadorRepository
        extends JpaRepository<Coordinador, Long> {

    Optional<Coordinador> findByUsuarioIdUsuario(Long idUsuario);
}
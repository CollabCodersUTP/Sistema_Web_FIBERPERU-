package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    Optional<Tecnico> findByUsuarioIdUsuario(Long idUsuario);

    List<Tecnico> findByEstadoTrueAndDisponibilidadTrue();
}
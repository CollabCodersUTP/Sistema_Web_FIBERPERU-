package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.NotificacionTecnico;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacionTecnicoRepository extends JpaRepository<NotificacionTecnico, Long> {
    List<NotificacionTecnico> findByUsuarioIdUsuarioOrderByFechaCreacionDescIdNotificacionDesc(
            Long idUsuario, Pageable pageable);

    Optional<NotificacionTecnico> findByIdNotificacionAndUsuarioIdUsuario(
            Long idNotificacion, Long idUsuario);
}

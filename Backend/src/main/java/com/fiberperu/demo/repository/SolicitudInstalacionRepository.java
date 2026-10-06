package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.SolicitudInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudInstalacionRepository
        extends JpaRepository<SolicitudInstalacion, Long> {

    Optional<SolicitudInstalacion> findByCodigoSolicitud(String codigoSolicitud);

    List<SolicitudInstalacion> findByClienteIdCliente(Long idCliente);

    List<SolicitudInstalacion> findByEstado(String estado);
}
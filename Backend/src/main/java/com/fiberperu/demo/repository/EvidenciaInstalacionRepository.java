package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.EvidenciaInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaInstalacionRepository
        extends JpaRepository<EvidenciaInstalacion, Long> {

    List<EvidenciaInstalacion> findByOrdenIdOrden(
            Long idOrden
    );

    List<EvidenciaInstalacion> findByEstadoValidacion(
            String estadoValidacion
    );
}
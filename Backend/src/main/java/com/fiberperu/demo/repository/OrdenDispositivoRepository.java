package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.OrdenDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenDispositivoRepository
        extends JpaRepository<OrdenDispositivo, Long> {

    List<OrdenDispositivo> findByOrdenIdOrden(
            Long idOrden
    );

    List<OrdenDispositivo> findByDispositivoIdDispositivo(
            Long idDispositivo
    );

    Optional<OrdenDispositivo>
    findByDispositivoIdDispositivoAndFechaRetiroIsNull(
            Long idDispositivo
    );
}
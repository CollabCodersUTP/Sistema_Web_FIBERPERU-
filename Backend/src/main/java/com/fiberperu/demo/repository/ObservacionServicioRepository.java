package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.ObservacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservacionServicioRepository
        extends JpaRepository<ObservacionServicio, Long> {

    List<ObservacionServicio> findByOrdenIdOrden(Long idOrden);
}
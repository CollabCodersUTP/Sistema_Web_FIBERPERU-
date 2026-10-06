package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.HistorialOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialOrdenRepository
        extends JpaRepository<HistorialOrden, Long> {

    List<HistorialOrden> findByOrdenIdOrdenOrderByFechaHoraAsc(
            Long idOrden
    );
}
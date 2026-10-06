package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface OrdenTrabajoRepository
        extends JpaRepository<OrdenTrabajo, Long> {

    Optional<OrdenTrabajo> findByCodigoOrden(String codigoOrden);

    Optional<OrdenTrabajo> findBySolicitudIdSolicitud(Long idSolicitud);

    List<OrdenTrabajo> findByTecnicoIdTecnico(Long idTecnico);

    List<OrdenTrabajo> findByCoordinadorIdCoordinador(Long idCoordinador);

    List<OrdenTrabajo> findByEstado(String estado);

    @Query("""
            select (count(o) > 0) from OrdenTrabajo o
            where o.tecnico.idTecnico = :idTecnico
              and o.fechaProgramada = :fecha
              and o.horaProgramada = :hora
              and o.idOrden <> :idOrden
              and o.estado not in ('FINALIZADA', 'CANCELADA')
            """)
    boolean existeConflictoHorario(
            @Param("idTecnico") Long idTecnico,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("idOrden") Long idOrden
    );
}

package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.InstallationOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstallationOrderRepository extends JpaRepository<InstallationOrder, Long> {
    Optional<InstallationOrder> findByCodigoOrden(String codigoOrden);
    List<InstallationOrder> findByEstado(String estado);
    List<InstallationOrder> findByTecnicoId(Long tecnicoId);
    List<InstallationOrder> findByClienteId(Long clienteId);
}

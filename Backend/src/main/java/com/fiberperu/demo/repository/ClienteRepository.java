package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    Optional<Cliente> findByUsuarioIdUsuario(Long idUsuario);

    boolean existsByNumeroDocumento(String numeroDocumento);
}

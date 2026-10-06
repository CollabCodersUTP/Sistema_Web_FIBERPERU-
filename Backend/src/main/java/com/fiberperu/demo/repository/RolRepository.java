package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Rol;
import com.fiberperu.demo.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RoleName nombre);
}
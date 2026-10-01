package com.fiberperu.demo.repository;

import com.fiberperu.demo.entity.Role;
import com.fiberperu.demo.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNombre(RoleName nombre);
}

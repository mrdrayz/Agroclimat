package com.agroclimate.repository;

import com.agroclimate.domain.entity.Role;
import com.agroclimate.domain.enums.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleCode code);
}

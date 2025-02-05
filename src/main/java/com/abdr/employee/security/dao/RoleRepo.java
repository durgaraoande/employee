package com.abdr.employee.security.dao;

import com.abdr.employee.security.entities.ERole;
import com.abdr.employee.security.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}

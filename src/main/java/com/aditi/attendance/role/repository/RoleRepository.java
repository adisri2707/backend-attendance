package com.aditi.attendance.role.repository;

import com.aditi.attendance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    List<Role> findByRoleNameContainingIgnoreCase(String roleName);

}
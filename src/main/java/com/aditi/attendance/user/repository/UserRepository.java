package com.aditi.attendance.user.repository;

import com.aditi.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployeeEmail(String email);

    Optional<User> findByEmployeeId(Long employeeId);

    boolean existsByUsername(String username);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    List<User> findByRoleId(Long roleId);

}
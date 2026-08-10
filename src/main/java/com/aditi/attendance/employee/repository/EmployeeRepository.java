package com.aditi.attendance.employee.repository;

import com.aditi.attendance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByActiveTrue();

    List<Employee> findByActiveFalse();

    List<Employee> findByDeletedFalse();

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

    Optional<Employee> findByIdAndActiveTrue(Long id);

    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    List<Employee> findByDepartmentContainingIgnoreCase(String department);

    List<Employee> findByDesignationContainingIgnoreCase(String designation);

}
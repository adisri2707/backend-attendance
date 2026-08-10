package com.aditi.attendance.employee.service;

import com.aditi.attendance.employee.dto.EmployeeRequest;
import com.aditi.attendance.employee.dto.EmployeeResponse;
import com.aditi.attendance.employee.exception.DuplicateEmployeeException;
import com.aditi.attendance.employee.exception.EmployeeNotFoundException;
import com.aditi.attendance.employee.mapper.EmployeeMapper;
import com.aditi.attendance.employee.repository.EmployeeCriteriaRepository;
import com.aditi.attendance.employee.repository.EmployeeRepository;
import com.aditi.attendance.common.util.EmployeeCodeGenerator;
import com.aditi.attendance.entity.Employee;
import com.aditi.attendance.entity.Role;
import com.aditi.attendance.entity.User;
import com.aditi.attendance.role.repository.RoleRepository;
import com.aditi.attendance.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeCriteriaRepository employeeCriteriaRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        validateDuplicateEmail(request.getEmail());
        validateDuplicatePhone(request.getPhoneNumber());

        Employee employee = EmployeeMapper.toEntity(request);
        employee.setActive(true);
        employee.setDeleted(false);
        employee.setEmployeeCode(EmployeeCodeGenerator.generate());

        Employee savedEmployee = employeeRepository.save(employee);

        Role employeeRole = roleRepository.findByRoleName("EMPLOYEE")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("EMPLOYEE")
                        .description("Employee role")
                        .build()));

        User user = User.builder()
            .username(savedEmployee.getEmail())
            .password("")
            .active(true)
            .employee(savedEmployee)
            .role(employeeRole)
            .build();

        userRepository.save(user);

        return EmployeeMapper.toResponse(savedEmployee);
    }

    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findByDeletedFalse()
            .stream()
            .map(EmployeeMapper::toResponse)
            .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee = findEmployeeById(id);

        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {

        Employee employee = findEmployeeById(id);

        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())) {
            validateDuplicateEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(employee.getPhoneNumber())) {
            validateDuplicatePhone(request.getPhoneNumber());
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());
        if (request.getActive() != null) {
            employee.setActive(request.getActive());
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        return EmployeeMapper.toResponse(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = findEmployeeById(id);

        // soft-delete: mark deleted and inactive
        employee.setDeleted(true);
        employee.setActive(false);

        employeeRepository.save(employee);
    }

    public List<EmployeeResponse> searchEmployee(String keyword) {

        return employeeCriteriaRepository.searchEmployees(keyword)
                .stream()
                .map(EmployeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Employee findEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with ID " + id + " not found."
                        ));
        if (employee.getDeleted() != null && employee.getDeleted()) {
            throw new EmployeeNotFoundException(
                "Employee with ID " + id + " is deleted."
            );
        }
        return employee;
    }

    private void validateDuplicateEmail(String email) {

        if (employeeRepository.existsByEmail(email)) {
            throw new DuplicateEmployeeException(
                    "Employee with email '" + email + "' already exists."
            );
        }
    }

    private void validateDuplicatePhone(String phone) {
        if (phone != null && employeeRepository.existsByPhoneNumber(phone)) {
            throw new DuplicateEmployeeException(
                    "Phone number '" + phone + "' already exists."
            );
        }
    }

}

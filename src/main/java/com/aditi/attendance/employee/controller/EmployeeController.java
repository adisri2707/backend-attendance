package com.aditi.attendance.employee.controller;

import com.aditi.attendance.employee.dto.EmployeeRequest;
import com.aditi.attendance.employee.dto.EmployeeResponse;
import com.aditi.attendance.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestParam String actorEmail,
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse response = employeeService.createEmployee(request, actorEmail);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {

        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /** Active employees for the employee "My Team" view. */
    @GetMapping("/team")
    public ResponseEntity<List<EmployeeResponse>> getTeamMembers() {

        return ResponseEntity.ok(employeeService.getTeamMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @PathVariable Long id) {

        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestParam String actorEmail,
            @Valid @RequestBody EmployeeRequest request) {

        return ResponseEntity.ok(
                employeeService.updateEmployee(id, request, actorEmail)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long id,
            @RequestParam String actorEmail) {

        employeeService.deleteEmployee(id, actorEmail);

        return ResponseEntity.ok("Employee deleted successfully.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponse>> searchEmployee(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                employeeService.searchEmployee(keyword)
        );
    }

}

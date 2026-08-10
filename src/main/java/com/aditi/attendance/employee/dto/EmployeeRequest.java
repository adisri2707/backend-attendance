package com.aditi.attendance.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String email;

    private String phoneNumber;

    private String department;

    private String designation;

    private LocalDate dateOfJoining;

    private Boolean active;

}
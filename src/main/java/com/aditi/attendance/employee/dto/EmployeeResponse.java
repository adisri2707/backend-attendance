package com.aditi.attendance.employee.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeResponse {

    private Long id;

    private String employeeCode;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String department;

    private String designation;

    private LocalDate dateOfJoining;

    private Boolean active;

    private Boolean deleted;

}
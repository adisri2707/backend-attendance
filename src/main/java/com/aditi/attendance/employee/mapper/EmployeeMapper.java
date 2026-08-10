package com.aditi.attendance.employee.mapper;

import com.aditi.attendance.employee.dto.EmployeeRequest;
import com.aditi.attendance.employee.dto.EmployeeResponse;
import com.aditi.attendance.entity.Employee;

public class EmployeeMapper {

    public static Employee toEntity(EmployeeRequest request) {

        Employee employee = new Employee();

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setActive(request.getActive());
        // by default, if not provided, ensure deleted is false
        employee.setDeleted(false);

        return employee;
    }

    public static EmployeeResponse toResponse(Employee employee) {

        EmployeeResponse response = new EmployeeResponse();

        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhoneNumber(employee.getPhoneNumber());
        response.setDepartment(employee.getDepartment());
        response.setDesignation(employee.getDesignation());
        response.setDateOfJoining(employee.getDateOfJoining());
        response.setActive(employee.getActive());
        response.setDeleted(employee.getDeleted());

        return response;
    }

}
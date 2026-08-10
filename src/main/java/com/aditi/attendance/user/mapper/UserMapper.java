package com.aditi.attendance.user.mapper;

import com.aditi.attendance.entity.Employee;
import com.aditi.attendance.entity.Role;
import com.aditi.attendance.entity.User;
import com.aditi.attendance.user.dto.UserRequest;
import com.aditi.attendance.user.dto.UserResponse;

public class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequest request,
                                Employee employee,
                                Role role) {

        return User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .active(request.getActive())
                .employee(employee)
                .role(role)
                .build();
    }

    public static UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .active(user.getActive())

                .roleId(user.getRole().getId())
                .roleName(user.getRole().getRoleName())

                .employeeId(user.getEmployee().getId())
                .employeeCode(user.getEmployee().getEmployeeCode())
                .employeeName(
                        user.getEmployee().getFirstName()
                                + " "
                                + user.getEmployee().getLastName()
                )
                .build();
    }
}
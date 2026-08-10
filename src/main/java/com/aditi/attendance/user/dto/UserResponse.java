package com.aditi.attendance.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private Boolean active;

    private Long roleId;

    private String roleName;

    private Long employeeId;

    private String employeeCode;

    private String employeeName;

}
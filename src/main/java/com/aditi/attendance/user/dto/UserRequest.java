package com.aditi.attendance.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotNull(message = "Role ID is required.")
    private Long roleId;

    @NotNull(message = "Employee ID is required.")
    private Long employeeId;

    @NotNull(message = "Active status is required.")
    private Boolean active;
}
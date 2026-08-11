package com.aditi.attendance.attendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunchRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Valid email is required.")
    private String email;

    @NotNull(message = "Status id is required.")
    private Integer statusId;
}

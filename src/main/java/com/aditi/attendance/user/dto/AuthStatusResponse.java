package com.aditi.attendance.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthStatusResponse {

    /** True only when no users exist yet — first signup becomes ADMIN. */
    private boolean signupAllowed;
}

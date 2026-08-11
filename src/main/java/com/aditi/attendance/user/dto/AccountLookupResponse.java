package com.aditi.attendance.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLookupResponse {

    private boolean found;

    private boolean needsPasswordSetup;

    private String email;
}

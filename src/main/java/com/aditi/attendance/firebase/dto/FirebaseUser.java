package com.aditi.attendance.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUser {

    private String uid;

    private String email;

    private String name;

    private boolean emailVerified;

    private String role;

}
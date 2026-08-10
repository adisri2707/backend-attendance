package com.aditi.attendance.role.mapper;

import com.aditi.attendance.entity.Role;
import com.aditi.attendance.role.dto.RoleRequest;
import com.aditi.attendance.role.dto.RoleResponse;

public class RoleMapper {

    private RoleMapper() {
    }

    public static Role toEntity(RoleRequest request) {

        return Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .build();
    }

    public static RoleResponse toResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }
}
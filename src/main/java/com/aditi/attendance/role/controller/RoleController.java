package com.aditi.attendance.role.controller;

import com.aditi.attendance.role.dto.RoleRequest;
import com.aditi.attendance.role.dto.RoleResponse;
import com.aditi.attendance.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) {

        return roleService.createRole(request);
    }

    @GetMapping
    public List<RoleResponse> getAllRoles() {

        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public RoleResponse getRoleById(@PathVariable Long id) {

        return roleService.getRoleById(id);
    }

    @PutMapping("/{id}")
    public RoleResponse updateRole(@PathVariable Long id,
                                   @Valid @RequestBody RoleRequest request) {

        return roleService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {

        roleService.deleteRole(id);
    }

    @GetMapping("/search")
    public List<RoleResponse> searchRole(@RequestParam String keyword) {

        return roleService.searchRole(keyword);
    }
}
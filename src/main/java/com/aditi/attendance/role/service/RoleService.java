package com.aditi.attendance.role.service;

import com.aditi.attendance.entity.Role;
import com.aditi.attendance.role.dto.RoleRequest;
import com.aditi.attendance.role.dto.RoleResponse;
import com.aditi.attendance.role.exception.DuplicateRoleException;
import com.aditi.attendance.role.exception.RoleNotFoundException;
import com.aditi.attendance.role.mapper.RoleMapper;
import com.aditi.attendance.role.repository.RoleCriteriaRepository;
import com.aditi.attendance.role.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleCriteriaRepository roleCriteriaRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {

        validateDuplicateRole(request.getRoleName());

        Role role = RoleMapper.toEntity(request);

        Role savedRole = roleRepository.save(role);

        return RoleMapper.toResponse(savedRole);
    }

    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(RoleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Long id) {

        Role role = findRoleById(id);

        return RoleMapper.toResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {

        Role role = findRoleById(id);

        if (!role.getRoleName().equalsIgnoreCase(request.getRoleName())) {
            validateDuplicateRole(request.getRoleName());
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());

        Role updatedRole = roleRepository.save(role);

        return RoleMapper.toResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(Long id) {

        Role role = findRoleById(id);

        roleRepository.delete(role);
    }

    public List<RoleResponse> searchRole(String keyword) {

        return roleCriteriaRepository.searchRole(keyword)
                .stream()
                .map(RoleMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Role findRoleById(Long id) {

        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role with ID " + id + " not found."
                        ));
    }

    private void validateDuplicateRole(String roleName) {

        if (roleRepository.existsByRoleName(roleName)) {

            throw new DuplicateRoleException(
                    "Role '" + roleName + "' already exists."
            );
        }
    }

}
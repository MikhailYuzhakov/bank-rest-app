package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleByRoleName(String roleName) {
        return roleRepository.findRoleByRoleName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
    }
}

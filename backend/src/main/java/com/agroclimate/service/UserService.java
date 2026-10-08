package com.agroclimate.service;

import com.agroclimate.domain.entity.AppUser;
import com.agroclimate.domain.entity.Role;
import com.agroclimate.domain.enums.RoleCode;
import com.agroclimate.domain.enums.UserStatus;
import com.agroclimate.dto.UpdateUserRolesRequest;
import com.agroclimate.dto.UserDto;
import com.agroclimate.exception.NotFoundException;
import com.agroclimate.repository.AppUserRepository;
import com.agroclimate.repository.RoleRepository;
import com.agroclimate.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EntityMapper mapper;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(mapper::toUserDto).toList();
    }

    public UserDto findById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return mapper.toUserDto(user);
    }

    @Transactional
    public UserDto changeStatus(Long id, UserStatus status) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        user.setStatus(status);
        return mapper.toUserDto(user);
    }

    @Transactional
    public UserDto updateRoles(Long id, UpdateUserRolesRequest req) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        Set<Role> roles = new HashSet<>();
        for (RoleCode roleCode : req.roles()) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new NotFoundException("Role not found: " + roleCode));
            roles.add(role);
        }

        user.setRoles(roles);
        return mapper.toUserDto(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}

package com.agroclimate.dto;

import com.agroclimate.domain.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String fullName,
        UserStatus status,
        LocalDateTime createdAt,
        Set<String> roles
) {}

package com.agroclimate.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для регистрации.
 * Роль пользователю назначается сервером (всегда VIEWER) и не передаётся клиентом.
 */
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password,
        String fullName
) {}

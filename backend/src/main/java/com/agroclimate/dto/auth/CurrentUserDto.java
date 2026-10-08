package com.agroclimate.dto.auth;

import java.util.List;

/**
 * Текущий пользователь (без токена) — для синхронизации ролей с клиентом.
 */
public record CurrentUserDto(
        Long userId,
        String email,
        String fullName,
        List<String> roles,
        GreenhouseAccessSummaryDto greenhouseAccess
) {}

package com.agroclimate.dto;

import java.time.LocalDateTime;

public record GreenhouseAssignmentDto(
        long greenhouseId,
        String greenhouseName,
        long userId,
        String userEmail,
        String accessLevel,
        LocalDateTime assignedAt
) {}

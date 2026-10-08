package com.agroclimate.dto;

import com.agroclimate.domain.enums.AccessLevel;
import jakarta.validation.constraints.NotNull;

public record UpsertGreenhouseAssignmentRequest(
        @NotNull Long greenhouseId,
        @NotNull Long userId,
        @NotNull AccessLevel accessLevel
) {}

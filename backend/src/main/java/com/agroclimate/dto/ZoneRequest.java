package com.agroclimate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ZoneRequest(
        @NotNull Long greenhouseId,
        @NotBlank String name,
        String description,
        BigDecimal areaM2
) {}

package com.agroclimate.dto;

import java.math.BigDecimal;

public record ZoneDto(
        Long id,
        Long greenhouseId,
        String name,
        String description,
        BigDecimal areaM2
) {}

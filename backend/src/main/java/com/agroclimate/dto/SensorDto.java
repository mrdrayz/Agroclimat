package com.agroclimate.dto;

import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.SensorStatus;

import java.time.LocalDateTime;

public record SensorDto(
        Long id,
        Long zoneId,
        Metric metric,
        String unit,
        String name,
        SensorStatus status,
        LocalDateTime installedAt
) {}

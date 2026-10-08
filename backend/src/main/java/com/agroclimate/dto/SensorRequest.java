package com.agroclimate.dto;

import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.SensorStatus;
import jakarta.validation.constraints.NotNull;

public record SensorRequest(
        @NotNull Long zoneId,
        @NotNull Metric metric,
        String name,
        SensorStatus status
) {}

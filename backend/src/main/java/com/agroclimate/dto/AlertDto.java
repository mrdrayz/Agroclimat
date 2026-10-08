package com.agroclimate.dto;

import com.agroclimate.domain.enums.AlertStatus;
import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.Severity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertDto(
        Long id,
        Long ruleId,
        Long sensorId,
        Metric metric,
        Severity severity,
        AlertStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        BigDecimal lastValue,
        String unit,
        String message,
        Long greenhouseId,
        String greenhouseName,
        Long zoneId,
        String zoneName
) {}

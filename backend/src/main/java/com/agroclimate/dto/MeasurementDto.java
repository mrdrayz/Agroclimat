package com.agroclimate.dto;

import com.agroclimate.domain.enums.MeasurementSource;
import com.agroclimate.domain.enums.Metric;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Каждое измерение содержит unit рядом с числовым значением — как требует ТЗ.
 */
public record MeasurementDto(
        Long id,
        Long sensorId,
        Metric metric,
        BigDecimal value,
        String unit,
        LocalDateTime ts,
        MeasurementSource source
) {}

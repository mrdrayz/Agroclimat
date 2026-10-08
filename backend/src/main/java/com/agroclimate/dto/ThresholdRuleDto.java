package com.agroclimate.dto;

import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.Severity;

import java.math.BigDecimal;

public record ThresholdRuleDto(
        Long id,
        Long zoneId,
        Metric metric,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue,
        Boolean enabled,
        Severity severity
) {}

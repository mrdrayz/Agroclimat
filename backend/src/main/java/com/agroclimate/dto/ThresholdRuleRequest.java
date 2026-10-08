package com.agroclimate.dto;

import com.agroclimate.domain.enums.Metric;
import com.agroclimate.domain.enums.Severity;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ThresholdRuleRequest(
        @NotNull Long zoneId,
        @NotNull Metric metric,
        BigDecimal minValue,
        BigDecimal maxValue,
        Boolean enabled,
        @NotNull Severity severity
) {}

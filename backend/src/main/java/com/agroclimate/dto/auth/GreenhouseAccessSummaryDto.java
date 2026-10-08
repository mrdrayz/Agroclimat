package com.agroclimate.dto.auth;

import java.util.List;

/**
 * Если {@code scoped=false} — назначений нет (или админ): данные по всем теплицам по глобальной роли.
 * Если {@code scoped=true} — только перечисленные теплицы.
 */
public record GreenhouseAccessSummaryDto(boolean scoped, List<GreenhouseAccessEntryDto> entries) {}

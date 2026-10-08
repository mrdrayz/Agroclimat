package com.agroclimate.dto;

import jakarta.validation.constraints.NotBlank;

public record GreenhouseRequest(
        @NotBlank String name,
        String location
) {}

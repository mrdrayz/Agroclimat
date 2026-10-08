package com.agroclimate.dto;

import com.agroclimate.domain.enums.RoleCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty Set<@NotNull RoleCode> roles
) {}

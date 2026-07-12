package com.transitops.backend.auth.dto;

import java.util.Set;

import com.transitops.backend.auth.model.RoleName;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRolesRequest(@NotEmpty Set<RoleName> roles) {
}

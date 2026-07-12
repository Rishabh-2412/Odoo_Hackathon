package com.transitops.backend.auth.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.auth.dto.CreateUserRequest;
import com.transitops.backend.auth.dto.UpdateUserRolesRequest;
import com.transitops.backend.auth.dto.UpdateUserStatusRequest;
import com.transitops.backend.auth.dto.UserSummary;
import com.transitops.backend.auth.service.UserAdminService;
import com.transitops.backend.common.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserAdminService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary create(@Valid @RequestBody CreateUserRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<UserSummary> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public UserSummary get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/roles")
    public UserSummary updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return service.updateRoles(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserSummary updateStatus(@PathVariable Long id, @RequestBody UpdateUserStatusRequest request) {
        return service.updateStatus(id, request);
    }
}

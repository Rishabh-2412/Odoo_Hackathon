package com.transitops.backend.auth.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.auth.dto.CreateUserRequest;
import com.transitops.backend.auth.dto.UpdateUserRolesRequest;
import com.transitops.backend.auth.dto.UpdateUserStatusRequest;
import com.transitops.backend.auth.dto.UserSummary;
import com.transitops.backend.auth.model.Role;
import com.transitops.backend.auth.model.RoleName;
import com.transitops.backend.auth.model.User;
import com.transitops.backend.auth.repository.RoleRepository;
import com.transitops.backend.auth.repository.UserRepository;
import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.common.exception.DuplicateResourceException;
import com.transitops.backend.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserSummary create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(resolveRoles(request.roles()));
        return UserMapper.toSummary(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummary> list(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable), UserMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public UserSummary get(Long id) {
        return UserMapper.toSummary(requireUser(id));
    }

    @Transactional
    public UserSummary updateRoles(Long id, UpdateUserRolesRequest request) {
        User user = requireUser(id);
        user.setRoles(resolveRoles(request.roles()));
        return UserMapper.toSummary(userRepository.save(user));
    }

    @Transactional
    public UserSummary updateStatus(Long id, UpdateUserStatusRequest request) {
        User user = requireUser(id);
        user.setEnabled(request.enabled());
        if (!request.enabled()) {
            refreshTokenService.revokeAll(user);
        }
        return UserMapper.toSummary(userRepository.save(user));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Set<Role> resolveRoles(Set<RoleName> names) {
        return names.stream().map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new BusinessRuleException("ROLE_NOT_CONFIGURED", "Role is not configured: " + name)))
                .collect(Collectors.toSet());
    }
}

package com.transitops.backend.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.transitops.backend.enums.UserStatus;

public class TransitOpsUserPrincipal
        implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final boolean accountNonLocked;
    private final boolean mustChangePassword;
    private final long tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public TransitOpsUserPrincipal(
            Long userId,
            String fullName,
            String email,
            String passwordHash,
            UserStatus status,
            boolean accountNonLocked,
            boolean mustChangePassword,
            long tokenVersion,
            Collection<? extends GrantedAuthority> authorities) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.accountNonLocked = accountNonLocked;
        this.mustChangePassword = mustChangePassword;
        this.tokenVersion = tokenVersion;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
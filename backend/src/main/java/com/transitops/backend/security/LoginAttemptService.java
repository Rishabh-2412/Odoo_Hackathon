package com.transitops.backend.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.entity.User;
import com.transitops.backend.repository.UserRepository;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final int maximumAttempts;
    private final Duration lockDuration;

    public LoginAttemptService(
            UserRepository userRepository,
            @Value(
                "${app.security.max-failed-login-attempts:5}"
            )
            int maximumAttempts,
            @Value(
                "${app.security.account-lock-minutes:15}"
            )
            long lockMinutes) {

        if (maximumAttempts <= 0) {
            throw new IllegalArgumentException(
                "Maximum login attempts must be positive"
            );
        }

        if (lockMinutes <= 0) {
            throw new IllegalArgumentException(
                "Account lock duration must be positive"
            );
        }

        this.userRepository = userRepository;
        this.maximumAttempts = maximumAttempts;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String email) {

        String normalizedEmail =
            email.trim().toLowerCase(Locale.ROOT);

        userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .ifPresent(user -> {

                int attempts =
                    user.getFailedLoginAttempts() + 1;

                user.setFailedLoginAttempts(attempts);

                if (attempts >= maximumAttempts) {
                    user.setAccountLockedUntil(
                        Instant.now().plus(lockDuration)
                    );
                }

                userRepository.save(user);
            });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(Long userId) {

        User user = userRepository
            .findById(userId)
            .orElseThrow();

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(Instant.now());

        userRepository.save(user);
    }
}

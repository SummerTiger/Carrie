package com.vending.service;

import com.vending.entity.User;
import com.vending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.security.max-failed-login-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Transactional
    public void loginSucceeded(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void loginFailed(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            attempts++;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= maxFailedAttempts) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            }

            userRepository.save(user);
        });
    }

    public boolean isAccountLocked(User user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            // Lock period expired, reset
            unlockAccount(user);
            return false;
        }

        return true;
    }

    @Transactional
    public void unlockAccount(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }
}

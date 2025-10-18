package com.vending.controller;

import com.vending.dto.*;
import com.vending.entity.AuditLog;
import com.vending.entity.RefreshToken;
import com.vending.entity.User;
import com.vending.exception.BadRequestException;
import com.vending.repository.UserRepository;
import com.vending.security.JwtTokenProvider;
import com.vending.service.AuditLogService;
import com.vending.service.LoginAttemptService;
import com.vending.service.RateLimitService;
import com.vending.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        try {
            // Security: Rate limiting - Check if IP has exceeded login attempts
            String ipAddress = getClientIpAddress(request);
            if (!rateLimitService.tryConsume(ipAddress)) {
                auditLogService.logFailure(
                        AuditLog.ACTION_LOGIN_FAILED,
                        AuditLog.RESOURCE_USER,
                        loginRequest.username(),
                        "Rate limit exceeded from IP: " + ipAddress,
                        "Too many login attempts"
                );
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(null);
            }

            // Check if account is locked before attempting authentication
            User user = userRepository.findByUsername(loginRequest.username()).orElse(null);
            if (user != null && loginAttemptService.isAccountLocked(user)) {
                throw new LockedException("Account is locked due to multiple failed login attempts. Please try again later.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            User userDetails = (User) authentication.getPrincipal();

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails);

            // Record successful login
            loginAttemptService.loginSucceeded(loginRequest.username());

            // Security: Reset rate limit for this IP after successful login
            String ipAddress = getClientIpAddress(request);
            rateLimitService.resetRateLimit(ipAddress);

            // Log successful login
            auditLogService.log(
                    AuditLog.ACTION_LOGIN,
                    AuditLog.RESOURCE_USER,
                    userDetails.getId().toString(),
                    "User logged in successfully from IP: " + ipAddress
            );

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .type("Bearer")
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(roles)
                    .build());

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            loginAttemptService.loginFailed(loginRequest.username());

            // Log failed login attempt
            auditLogService.logFailure(
                    AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.RESOURCE_USER,
                    loginRequest.username(),
                    "Failed login attempt - invalid credentials",
                    "Invalid username or password"
            );

            throw new BadRequestException("Invalid username or password");
        } catch (LockedException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        if (!refreshToken.isValid()) {
            throw new BadRequestException("Refresh token is not valid");
        }

        User user = refreshToken.getUser();

        // Create new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        String newAccessToken = tokenProvider.generateToken(authentication);

        Set<String> roles = user.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.refreshToken());

        // Log logout
        auditLogService.log(
                AuditLog.ACTION_LOGOUT,
                null,
                null,
                "User logged out successfully"
        );

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenService.revokeAllUserTokens(user);

        // Log password change
        auditLogService.log(
                AuditLog.ACTION_PASSWORD_CHANGED,
                AuditLog.RESOURCE_USER,
                user.getId().toString(),
                "User password changed successfully"
        );

        return ResponseEntity.ok("Password changed successfully. Please login again.");
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (tokenProvider.validateToken(jwt)) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    /**
     * Extract client IP address from request, considering proxy headers.
     * Checks X-Forwarded-For, X-Real-IP headers for proxied requests.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}

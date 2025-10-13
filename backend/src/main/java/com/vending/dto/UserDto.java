package com.vending.dto;

import com.vending.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record UserDto(
    UUID id,
    @NotBlank String username,
    @Email String email,
    String firstName,
    String lastName,
    String phoneNumber,
    Set<UserRole> roles,
    boolean enabled,
    LocalDateTime lastLogin,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

package com.vending.dto;

import com.vending.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateUserRequest(
    @Email(message = "Email should be valid")
    String email,

    String firstName,

    String lastName,

    String phoneNumber,

    Set<UserRole> roles,

    Boolean enabled,

    Boolean accountNonLocked
) {}

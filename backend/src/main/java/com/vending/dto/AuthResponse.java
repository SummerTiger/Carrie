package com.vending.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record AuthResponse(
    String token,
    String type,
    String username,
    String email,
    Set<String> roles
) {
    public AuthResponse {
        if (type == null || type.isBlank()) {
            type = "Bearer";
        }
    }
}

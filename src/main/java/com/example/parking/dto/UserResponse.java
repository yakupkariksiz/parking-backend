package com.example.parking.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        boolean enabled
) {}

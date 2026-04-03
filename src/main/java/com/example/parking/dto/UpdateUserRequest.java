package com.example.parking.dto;

public record UpdateUserRequest(
        String email,
        String role,
        String password,
        Boolean enabled
) {}

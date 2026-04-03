package com.example.parking.dto;

public record CreateUserRequest(
        String username,
        String password,
        String email,
        String role
) {}

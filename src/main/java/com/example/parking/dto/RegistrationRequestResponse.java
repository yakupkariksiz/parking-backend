package com.example.parking.dto;

import java.time.LocalDateTime;

public record RegistrationRequestResponse(
        Long id,
        String email,
        String status,
        String approvedRole,
        LocalDateTime createdAt
) {
}

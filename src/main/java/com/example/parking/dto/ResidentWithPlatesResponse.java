package com.example.parking.dto;

import java.util.List;

public record ResidentWithPlatesResponse(
        Long id,
        String uniqueCode,
        String name,
        String address,
        List<String> licensePlates
) {}

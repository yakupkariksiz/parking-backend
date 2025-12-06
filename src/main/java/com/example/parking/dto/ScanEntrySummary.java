package com.example.parking.dto;

public record ScanEntrySummary(
        Long id,
        String licensePlate,
        boolean resident,
        String location,
        String createdAt
) {}


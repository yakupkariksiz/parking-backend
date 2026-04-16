package com.example.parking.dto;

public record BulkImportRowResult(
        int rowNumber,
        String uniqueCode,
        String licensePlate,
        boolean success,
        String errorReason
) {}

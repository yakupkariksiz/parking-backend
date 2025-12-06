package com.example.parking.dto;

public record ScanEntryRequest(
        String licensePlate,
        String location,
        String sessionName,
        Boolean noParking
) {}

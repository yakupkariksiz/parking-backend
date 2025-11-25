package com.example.parking.dto;

public record SessionStatsResponse(
        Long sessionId,
        String sessionName,
        long totalCount,
        long residentCount,
        long nonResidentCount,
        double residentPercentage,
        double nonResidentPercentage
) {}

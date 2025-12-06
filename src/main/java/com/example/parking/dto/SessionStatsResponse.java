package com.example.parking.dto;

import java.util.List;

public record SessionStatsResponse(
        Long sessionId,
        String sessionName,
        long totalCount,
        long residentCount,
        long nonResidentCount,
        double residentPercentage,
        double nonResidentPercentage,
        List<ScanEntrySummary> entries
) {}

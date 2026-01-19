package com.example.parking.service;

import com.example.parking.dto.OccupancyChartResponse;
import com.example.parking.dto.OccupancyDataPoint;
import com.example.parking.dto.ScanEntrySummary;
import com.example.parking.dto.SessionStatsResponse;
import com.example.parking.model.ScanEntry;
import com.example.parking.model.ScanSession;
import com.example.parking.repository.ScanEntryRepository;
import com.example.parking.repository.ScanSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final int PARKING_CAPACITY = 96;

    private final ScanEntryRepository scanEntryRepository;
    private final ScanSessionRepository scanSessionRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StatsService(ScanEntryRepository scanEntryRepository,
                        ScanSessionRepository scanSessionRepository) {
        this.scanEntryRepository = scanEntryRepository;
        this.scanSessionRepository = scanSessionRepository;
    }

    public SessionStatsResponse getSessionStats(Long sessionId) {
        ScanSession session = scanSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<ScanEntry> entries = scanEntryRepository.findBySessionId(sessionId);

        long total = entries.size();
        if (total == 0) {
            return new SessionStatsResponse(
                    session.getId(),
                    session.getName(),
                    0,
                    0,
                    0,
                    0.0,
                    0.0,
                    List.of()
            );
        }

        long residentCount = entries.stream()
                .filter(e -> Boolean.TRUE.equals(e.getResident()))
                .count();

        long nonResidentCount = total - residentCount;

        double residentPct = round(residentCount * 100.0 / total);
        double nonResidentPct = round(nonResidentCount * 100.0 / total);

        List<ScanEntrySummary> entrySummaries = entries.stream()
                .map(e -> new ScanEntrySummary(
                        e.getId(),
                        e.getLicensePlate(),
                        Boolean.TRUE.equals(e.getResident()),
                        e.getLocation(),
                        e.getCreatedAt() != null ? e.getCreatedAt().format(formatter) : null
                ))
                .toList();

        return new SessionStatsResponse(
                session.getId(),
                session.getName(),
                total,
                residentCount,
                nonResidentCount,
                residentPct,
                nonResidentPct,
                entrySummaries
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0; // 1 decimal
    }

    public OccupancyChartResponse getOccupancyChartData(LocalDate startDate, LocalDate endDate) {
        // Default to last 7 days if not specified
        final LocalDate finalEndDate = (endDate != null) ? endDate : LocalDate.now();
        final LocalDate finalStartDate = (startDate != null) ? startDate : finalEndDate.minusDays(6);

        // Get all scan entries in the date range
        List<ScanEntry> allEntries = scanEntryRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> {
                    LocalDate entryDate = e.getCreatedAt().toLocalDate();
                    return !entryDate.isBefore(finalStartDate) && !entryDate.isAfter(finalEndDate);
                })
                .toList();

        // Group entries by date
        Map<LocalDate, List<ScanEntry>> entriesByDate = allEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getCreatedAt().toLocalDate()));

        // Create data points for each day in the range
        List<OccupancyDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM dd");

        LocalDate currentDate = finalStartDate;
        int totalResidents = 0;
        int totalNonResidents = 0;
        int daysWithData = 0;

        while (!currentDate.isAfter(finalEndDate)) {
            List<ScanEntry> dayEntries = entriesByDate.getOrDefault(currentDate, List.of());

            int residentCount = (int) dayEntries.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getResident()))
                    .count();
            int nonResidentCount = dayEntries.size() - residentCount;

            dataPoints.add(new OccupancyDataPoint(
                    currentDate,
                    currentDate.format(labelFormatter),
                    residentCount,
                    nonResidentCount,
                    PARKING_CAPACITY
            ));

            if (!dayEntries.isEmpty()) {
                totalResidents += residentCount;
                totalNonResidents += nonResidentCount;
                daysWithData++;
            }

            currentDate = currentDate.plusDays(1);
        }

        // Calculate average
        OccupancyDataPoint average;
        if (daysWithData > 0) {
            int avgResidents = totalResidents / daysWithData;
            int avgNonResidents = totalNonResidents / daysWithData;
            average = new OccupancyDataPoint(null, "Average", avgResidents, avgNonResidents, PARKING_CAPACITY);
        } else {
            average = new OccupancyDataPoint(null, "Average", 0, 0, PARKING_CAPACITY);
        }

        return new OccupancyChartResponse(
                dataPoints,
                average,
                PARKING_CAPACITY,
                finalStartDate.toString(),
                finalEndDate.toString()
        );
    }
}

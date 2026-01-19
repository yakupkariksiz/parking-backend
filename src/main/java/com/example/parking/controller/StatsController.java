package com.example.parking.controller;

import com.example.parking.dto.OccupancyChartResponse;
import com.example.parking.dto.SessionStatsResponse;
import com.example.parking.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/session/{id}")
    public SessionStatsResponse getSessionStats(@PathVariable("id") Long sessionId) {
        return statsService.getSessionStats(sessionId);
    }

    @GetMapping("/occupancy")
    public OccupancyChartResponse getOccupancyChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statsService.getOccupancyChartData(startDate, endDate);
    }
}

package com.example.parking.controller;

import com.example.parking.dto.SessionStatsResponse;
import com.example.parking.service.StatsService;
import org.springframework.web.bind.annotation.*;

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
}

package com.example.parking.dto;

import java.util.List;

public record ResidentPlatesRequest(
        List<String> licensePlates
) {}

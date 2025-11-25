package com.example.parking.dto;

import java.util.List;

public record ResidentBulkDeleteRequest(
        List<Long> ids
) {}

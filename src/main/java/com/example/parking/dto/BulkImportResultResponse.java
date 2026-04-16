package com.example.parking.dto;

import java.util.List;

public record BulkImportResultResponse(
        int totalRows,
        int successCount,
        int failedCount,
        List<BulkImportRowResult> rows
) {}

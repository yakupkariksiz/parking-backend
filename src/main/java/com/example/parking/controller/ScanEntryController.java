package com.example.parking.controller;

import com.example.parking.dto.ScanEntryRequest;
import com.example.parking.service.ScanEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scan-entries")
public class ScanEntryController {

    private final ScanEntryService scanEntryService;

    public ScanEntryController(ScanEntryService scanEntryService) {
        this.scanEntryService = scanEntryService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ScanEntryRequest request) {
        scanEntryService.createScanEntry(request);
        return ResponseEntity.ok().build();
    }
}

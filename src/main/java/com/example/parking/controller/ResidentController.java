package com.example.parking.controller;

import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.service.ResidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping("/{uniqueCode}/plates")
    public ResponseEntity<Void> addPlates(
            @PathVariable String uniqueCode,
            @RequestBody ResidentPlatesRequest request
    ) {
        residentService.addOrUpdatePlatesForResident(uniqueCode, request);
        return ResponseEntity.ok().build();
    }
}

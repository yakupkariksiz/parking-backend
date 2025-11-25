package com.example.parking.controller;

import com.example.parking.dto.ResidentBulkDeleteRequest;
import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.dto.ResidentWithPlatesResponse;
import com.example.parking.service.ResidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<ResidentWithPlatesResponse> listResidents() {
        return residentService.getAllResidentsWithPlates();
    }

    @DeleteMapping("/{residentId}")
    public ResponseEntity<Void> deleteResident(@PathVariable Long residentId) {
        residentService.deleteResident(residentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody ResidentBulkDeleteRequest request) {
        residentService.deleteResidents(request.ids());
        return ResponseEntity.noContent().build();
    }

}

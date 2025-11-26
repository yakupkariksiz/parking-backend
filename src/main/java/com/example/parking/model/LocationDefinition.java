package com.example.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "location_definition")
public class LocationDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Örn: "NORTH-01"
    @Column(nullable = false, unique = true)
    private String code;

    // Örn: "NORTH" veya "SOUTH"
    @Column(nullable = false)
    private String area;

    // Örn: 1, 2, 3...
    @Column(nullable = false)
    private Integer spotNumber;

    public LocationDefinition() {
    }

    public LocationDefinition(String code, String area, Integer spotNumber) {
        this.code = code;
        this.area = area;
        this.spotNumber = spotNumber;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getArea() {
        return area;
    }

    public Integer getSpotNumber() {
        return spotNumber;
    }
}

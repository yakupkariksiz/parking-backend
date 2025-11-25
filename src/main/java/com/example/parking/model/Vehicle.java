package com.example.parking.model;

import jakarta.persistence.*;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @ManyToOne(optional = false)
    private Resident resident;

    public Vehicle() {
    }

    public Vehicle(String licensePlate, Resident resident) {
        this.licensePlate = licensePlate;
        this.resident = resident;
    }

    public Long getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }
}

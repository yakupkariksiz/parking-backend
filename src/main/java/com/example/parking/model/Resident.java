package com.example.parking.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uniqueCode;

    private String name;

    private String address;

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    public Resident() {
    }

    public Resident(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public Long getId() {
        return id;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setResident(this);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
        vehicle.setResident(null);
    }
}

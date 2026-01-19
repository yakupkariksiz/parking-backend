package com.example.parking.dto;

import java.time.LocalDate;

public class OccupancyDataPoint {
    private LocalDate date;
    private String label;
    private int residentCount;
    private int nonResidentCount;
    private int totalCount;
    private double occupancyPercentage;
    private boolean overCapacity;

    public OccupancyDataPoint() {
    }

    public OccupancyDataPoint(LocalDate date, String label, int residentCount, int nonResidentCount, int capacity) {
        this.date = date;
        this.label = label;
        this.residentCount = residentCount;
        this.nonResidentCount = nonResidentCount;
        this.totalCount = residentCount + nonResidentCount;
        this.occupancyPercentage = capacity > 0 ? (this.totalCount * 100.0 / capacity) : 0;
        this.overCapacity = this.totalCount > capacity;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getResidentCount() {
        return residentCount;
    }

    public void setResidentCount(int residentCount) {
        this.residentCount = residentCount;
    }

    public int getNonResidentCount() {
        return nonResidentCount;
    }

    public void setNonResidentCount(int nonResidentCount) {
        this.nonResidentCount = nonResidentCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public boolean isOverCapacity() {
        return overCapacity;
    }

    public void setOverCapacity(boolean overCapacity) {
        this.overCapacity = overCapacity;
    }
}


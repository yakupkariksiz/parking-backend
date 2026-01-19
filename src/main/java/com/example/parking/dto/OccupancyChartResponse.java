package com.example.parking.dto;

import java.util.List;

public class OccupancyChartResponse {
    private List<OccupancyDataPoint> dataPoints;
    private OccupancyDataPoint average;
    private int capacity;
    private String periodStart;
    private String periodEnd;

    public OccupancyChartResponse() {
    }

    public OccupancyChartResponse(List<OccupancyDataPoint> dataPoints, OccupancyDataPoint average, int capacity, String periodStart, String periodEnd) {
        this.dataPoints = dataPoints;
        this.average = average;
        this.capacity = capacity;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public List<OccupancyDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<OccupancyDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public OccupancyDataPoint getAverage() {
        return average;
    }

    public void setAverage(OccupancyDataPoint average) {
        this.average = average;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }
}


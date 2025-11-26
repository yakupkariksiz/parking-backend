package com.example.parking;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ParkingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingBackendApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Tüm uygulama için default timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
    }
}

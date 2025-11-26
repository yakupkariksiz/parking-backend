package com.example.parking.config;

import com.example.parking.model.LocationDefinition;
import com.example.parking.repository.LocationDefinitionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocationDefinitionInitializer {

    @Bean
    CommandLineRunner initLocations(LocationDefinitionRepository locationRepo) {
        return args -> {
            if (locationRepo.count() > 0) {
                return; // zaten dolu, dokunma
            }

            // Burada kaç spot olacağını sen belirlersin
            int spotsPerSideNorth = 41; // istersen 10/50 vs yapabilirsin
            int spotsPerSideSouth = 55; // istersen 10/50 vs yapabilirsin

            for (int i = 1; i <= spotsPerSideNorth; i++) {
                String padded = String.format("%02d", i);

                String northCode = "NORTH-" + padded;

                locationRepo.save(new LocationDefinition(northCode, "NORTH", i));
            }

            for (int i = 1; i <= spotsPerSideSouth; i++) {
                String padded = String.format("%02d", i);

                String northCode = "SOUTH-" + padded;

                locationRepo.save(new LocationDefinition(northCode, "SOUTH", i));
            }
        };
    }
}

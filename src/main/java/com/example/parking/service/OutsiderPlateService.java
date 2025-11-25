package com.example.parking.service;

import com.example.parking.model.OutsiderPlate;
import com.example.parking.repository.OutsiderPlateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutsiderPlateService {

    private final OutsiderPlateRepository outsiderPlateRepository;

    public OutsiderPlateService(OutsiderPlateRepository outsiderPlateRepository) {
        this.outsiderPlateRepository = outsiderPlateRepository;
    }

    @Transactional
    public void registerOutsider(String normalizedPlate) {
        outsiderPlateRepository.findByLicensePlate(normalizedPlate)
                .ifPresentOrElse(
                        OutsiderPlate::seenAgain,
                        () -> outsiderPlateRepository.save(new OutsiderPlate(normalizedPlate))
                );
    }
}

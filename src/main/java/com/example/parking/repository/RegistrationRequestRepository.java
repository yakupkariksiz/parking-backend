package com.example.parking.repository;

import com.example.parking.model.RegistrationRequest;
import com.example.parking.model.RegistrationRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByEmail(String email);
    Optional<RegistrationRequest> findByToken(String token);
    List<RegistrationRequest> findByStatusOrderByCreatedAtDesc(Status status);
    List<RegistrationRequest> findAllByOrderByCreatedAtDesc();
}

package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    //TODO: sa vad daca imi mai e utila metoda asta cand fac autentificarea
    Optional<Patient> findByUserEmail(String email);
}
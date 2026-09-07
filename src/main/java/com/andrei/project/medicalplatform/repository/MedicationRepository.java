package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
}
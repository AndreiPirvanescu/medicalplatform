package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatient_Id(Long patientId);
}
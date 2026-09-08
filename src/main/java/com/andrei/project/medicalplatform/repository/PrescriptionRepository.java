package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long>,
        JpaSpecificationExecutor<Prescription> {

    List<Prescription> findByPatient_Id(Long patientId);
}
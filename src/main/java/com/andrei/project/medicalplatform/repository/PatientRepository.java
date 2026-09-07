package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository extends JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {

    boolean existsByUser_Id(Long userId);
}
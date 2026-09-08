package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByPatient_Id(Long patientId);

    boolean existsByPatient_Id(Long patientId);
}
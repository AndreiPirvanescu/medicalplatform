package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long>,
        JpaSpecificationExecutor<Doctor> {

    boolean existsByUser_Id(Long userId);

    List<Doctor> findByMedicalUnit_Id(Long medicalUnitId);

    List<Doctor> findBySpecializationsContaining(Specialization specialization);
}
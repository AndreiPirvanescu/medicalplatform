package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
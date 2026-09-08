package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.MedicalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MedicalUnitRepository extends JpaRepository<MedicalUnit, Long>,
        JpaSpecificationExecutor<MedicalUnit> {

    Optional<MedicalUnit> findByManager_Id(Long managerId);

    boolean existsByManager_Id(Long managerId);

    boolean existsByEmail(String email);
}
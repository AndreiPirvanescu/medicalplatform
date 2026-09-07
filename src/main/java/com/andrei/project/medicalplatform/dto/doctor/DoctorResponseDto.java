package com.andrei.project.medicalplatform.dto.doctor;

import com.andrei.project.medicalplatform.model.Specialization;

import java.util.List;

public record DoctorResponseDto(
        Long id,
        Long medicalUnitId,
        String medicalUnitName,
        Long userId,
        String doctorFullName,
        String email,
        String licenseNumber,
        List<Specialization> specializations
) {}
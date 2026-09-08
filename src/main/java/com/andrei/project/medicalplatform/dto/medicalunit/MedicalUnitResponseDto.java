package com.andrei.project.medicalplatform.dto.medicalunit;

public record MedicalUnitResponseDto(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        Long managerId,
        String managerFullName,
        int doctorCount
) {}
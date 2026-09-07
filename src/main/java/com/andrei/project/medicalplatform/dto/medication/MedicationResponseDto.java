package com.andrei.project.medicalplatform.dto.medication;

public record MedicationResponseDto(
        Long id,
        String name,
        String dosage,
        String description
) {}
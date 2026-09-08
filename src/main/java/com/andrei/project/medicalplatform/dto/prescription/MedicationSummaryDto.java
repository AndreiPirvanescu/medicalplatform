package com.andrei.project.medicalplatform.dto.prescription;

public record MedicationSummaryDto(
        Long id,
        String name,
        String dosage
) {}
package com.andrei.project.medicalplatform.dto.prescription;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescriptionUpdateDto(
        String notes,

        @NotEmpty(message = "At least one medication is required")
        List<Long> medicationIds
) {}
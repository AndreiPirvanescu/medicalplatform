package com.andrei.project.medicalplatform.dto.prescription;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescriptionRequestDto(
        Long appointmentId,

        String notes,

        @NotEmpty(message = "At least one medication is required")
        List<Long> medicationIds
) {}
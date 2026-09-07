package com.andrei.project.medicalplatform.dto.medicalrecord;

import jakarta.validation.constraints.NotBlank;

public record MedicalRecordUpdateDto(
        @NotBlank(message = "Notes must not be blank")
        String notes
) {}
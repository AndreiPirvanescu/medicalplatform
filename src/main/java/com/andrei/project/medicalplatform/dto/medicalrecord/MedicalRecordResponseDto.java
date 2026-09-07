package com.andrei.project.medicalplatform.dto.medicalrecord;

import java.time.LocalDateTime;

public record MedicalRecordResponseDto(
        Long id,
        Long patientId,
        String patientFullName,
        LocalDateTime createdDate,
        String notes
) {}
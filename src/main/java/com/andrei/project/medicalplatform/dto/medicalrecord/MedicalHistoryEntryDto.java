package com.andrei.project.medicalplatform.dto.medicalrecord;

import java.time.LocalDateTime;

public record MedicalHistoryEntryDto(
        HistoryEntryType type,
        LocalDateTime date,
        Long doctorId,
        String doctorFullName,
        String description
) {}
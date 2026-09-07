package com.andrei.project.medicalplatform.dto.medication;

import java.time.LocalDateTime;

public record MedicationHistoryEntryDto(
        Long medicationId,
        String medicationName,
        String dosage,
        Long prescriptionId,
        Long doctorId,
        String doctorFullName,
        LocalDateTime issueDate,
        String notes
) {}
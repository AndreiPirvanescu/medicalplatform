package com.andrei.project.medicalplatform.dto.prescription;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponseDto(
        Long id,
        Long patientId,
        String patientFullName,
        Long doctorId,
        String doctorFullName,
        Long appointmentId,
        LocalDateTime issueDate,
        String notes,
        List<MedicationSummaryDto> medications
) {}
package com.andrei.project.medicalplatform.dto.patient;

import java.time.LocalDate;

public record PatientResponseDto(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String bloodType
) {}
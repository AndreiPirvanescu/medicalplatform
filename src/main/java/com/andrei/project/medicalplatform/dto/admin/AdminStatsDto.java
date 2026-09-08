package com.andrei.project.medicalplatform.dto.admin;

public record AdminStatsDto(
        long totalMedicalUnits,
        long totalDoctors,
        long totalPatients,
        long totalAppointments,
        long totalPrescriptions
) {}
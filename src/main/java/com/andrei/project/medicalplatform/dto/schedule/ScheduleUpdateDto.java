package com.andrei.project.medicalplatform.dto.schedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleUpdateDto(
        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime,

        @NotNull(message = "Availability status is required")
        Boolean available
) {}
package com.andrei.project.medicalplatform.dto.schedule;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRequestDto(
        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime
) {}
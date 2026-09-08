package com.andrei.project.medicalplatform.dto.schedule;

import java.time.LocalDateTime;

public record ScheduleResponseDto(
        Long id,
        Long doctorId,
        String doctorFullName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean available
) {}
package com.andrei.project.medicalplatform.dto.specialization;

import com.andrei.project.medicalplatform.model.Specialization;

public record SpecializationDto(
        Specialization id,
        String name
) {}

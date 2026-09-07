package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.model.Medication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicationMapper {

    MedicationResponseDto toDto(Medication medication);
}
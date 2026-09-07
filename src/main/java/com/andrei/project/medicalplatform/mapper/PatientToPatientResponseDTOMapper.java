package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.patient.PatientResponseDTO;
import com.andrei.project.medicalplatform.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientToPatientResponseDTOMapper {

    public PatientResponseDTO mapToResponseDto(Patient patient) {
        return PatientResponseDTO.builder()
                .id(patient.getId())
                .userId(patient.getUser().getId())
                .email(patient.getUser().getEmail())
                .firstName(patient.getUser().getFirstName())
                .lastName(patient.getUser().getLastName())
//                .phoneNumber(patient.getPhoneNumber())
//                .birthDate(patient.getBirthDate())
                .bloodType(patient.getBloodType())
                .build();
    }
}
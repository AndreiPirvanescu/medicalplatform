package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDTO;
import com.andrei.project.medicalplatform.model.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorToDoctorResponseDTOMapper {

    public DoctorResponseDTO mapToResponseDto(Doctor doctor) {
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .email(doctor.getUser().getEmail())
                .firstName(doctor.getUser().getFirstName())
                .lastName(doctor.getUser().getLastName())
                .specializations(doctor.getSpecializations())
                .licenseNumber(doctor.getLicenseNumber())
                .build();
    }
}

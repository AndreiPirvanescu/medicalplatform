package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.specialization.SpecializationDto;
import com.andrei.project.medicalplatform.mapper.DoctorMapper;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecializationService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper mapper;

    public List<SpecializationDto> getAll() {
        return Arrays.stream(Specialization.values())
                .map(s -> new SpecializationDto(s, s.name()))
                .toList();
    }

    public List<DoctorResponseDto> getDoctorsBySpecialization(Specialization specialization) {
        return doctorRepository.findBySpecializationsContaining(specialization).stream()
                .map(mapper::toDto)
                .toList();
    }
}
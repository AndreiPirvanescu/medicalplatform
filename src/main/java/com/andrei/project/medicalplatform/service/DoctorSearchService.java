package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.mapper.DoctorMapper;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.spec.DoctorSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorSearchService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper mapper;

    public Page<DoctorResponseDto> search(String name, Specialization specialization, Long medicalUnitId,
                                          Boolean available, LocalDateTime from, LocalDateTime to,
                                          Pageable pageable) {
        return doctorRepository
                .findAll(DoctorSpecifications.filterBy(name, specialization, medicalUnitId, available, from, to),
                        pageable)
                .map(mapper::toDto);
    }
}
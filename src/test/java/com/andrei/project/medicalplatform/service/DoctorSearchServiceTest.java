package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.mapper.DoctorMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSearchServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DoctorMapper mapper;

    @InjectMocks
    private DoctorSearchService doctorSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void search_mapsPageOfResults_withAllFiltersProvided() {
        Doctor doctor = new Doctor();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        Page<DoctorResponseDto> result = doctorSearchService.search(
                "Ann", Specialization.CARDIOLOGIE, 1L, true,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 2, 0, 0), pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_mapsPageOfResults_withNoFiltersProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Doctor> page = new PageImpl<>(List.of());
        when(doctorRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<DoctorResponseDto> result = doctorSearchService.search(null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
    }
}

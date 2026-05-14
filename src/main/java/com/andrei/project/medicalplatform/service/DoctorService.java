package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.CreateDoctorRequestDTO;
import com.andrei.project.medicalplatform.dto.DoctorResponseDTO;
import com.andrei.project.medicalplatform.dto.UpdateDoctorRequestDTO;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public DoctorResponseDTO createDoctor(CreateDoctorRequestDTO dto) {

        doctorRepository.findByEmail(dto.getEmail())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Doctor email already exists");
                });

        Doctor doctor = Doctor.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .specialization(dto.getSpecialization())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        return mapToDTO(savedDoctor);
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public DoctorResponseDTO getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        return mapToDTO(doctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, UpdateDoctorRequestDTO dto) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        doctor.setName(dto.getName());
        doctor.setEmail(dto.getEmail());
        doctor.setSpecialization(dto.getSpecialization());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        return mapToDTO(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        doctorRepository.delete(doctor);
    }

    private DoctorResponseDTO mapToDTO(Doctor doctor) {

        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .specialization(doctor.getSpecialization())
                .build();
    }
}
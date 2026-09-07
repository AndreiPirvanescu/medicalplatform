package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.doctor.DoctorRequestDto;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.exception.SpecializationAlreadyExistsException;
import com.andrei.project.medicalplatform.exception.UserAlreadyDoctorException;
import com.andrei.project.medicalplatform.mapper.DoctorMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.MedicalUnit;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.MedicalUnitRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import com.andrei.project.medicalplatform.repository.spec.DoctorSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final MedicalUnitRepository medicalUnitRepository;
    private final UserRepository userRepository;
    private final DoctorMapper mapper;

    public DoctorResponseDto create(Long medicalUnitId, DoctorRequestDto request) {
        MedicalUnit medicalUnit = getMedicalUnitOrThrow(medicalUnitId);
        User user = getUserOrThrow(request.userId());

        if (doctorRepository.existsByUser_Id(user.getId())) {
            throw new UserAlreadyDoctorException(
                    "User with id " + user.getId() + " is already registered as a doctor");
        }

        Doctor doctor = new Doctor();
        doctor.setMedicalUnit(medicalUnit);
        doctor.setUser(user);
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setSpecializations(new ArrayList<>(request.specializations()));

        return mapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorResponseDto update(Long id, DoctorRequestDto request) {
        Doctor doctor = getDoctorOrThrow(id);

        if (!doctor.getUser().getId().equals(request.userId())) {
            if (doctorRepository.existsByUser_Id(request.userId())) {
                throw new UserAlreadyDoctorException(
                        "User with id " + request.userId() + " is already registered as a doctor");
            }
            doctor.setUser(getUserOrThrow(request.userId()));
        }

        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setSpecializations(new ArrayList<>(request.specializations()));

        return mapper.toDto(doctorRepository.save(doctor));
    }

    public void delete(Long id) {
        doctorRepository.delete(getDoctorOrThrow(id));
    }

    @Transactional(readOnly = true)
    public DoctorResponseDto getById(Long id) {
        return mapper.toDto(getDoctorOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDto> getAll(String name, Specialization specialization,
                                          Long medicalUnitId, Pageable pageable) {
        return doctorRepository
                .findAll(DoctorSpecifications.filterByShort(name, specialization, medicalUnitId), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDto> getByMedicalUnit(Long medicalUnitId) {
        getMedicalUnitOrThrow(medicalUnitId);
        return doctorRepository.findByMedicalUnit_Id(medicalUnitId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public DoctorResponseDto addSpecialization(Long doctorId, Specialization specialization) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        if (doctor.getSpecializations().contains(specialization)) {
            throw new SpecializationAlreadyExistsException(
                    "Doctor already has specialization " + specialization);
        }
        doctor.getSpecializations().add(specialization);
        return mapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorResponseDto removeSpecialization(Long doctorId, Specialization specialization) {
        Doctor doctor = getDoctorOrThrow(doctorId);
        if (!doctor.getSpecializations().remove(specialization)) {
            throw new EntityNotFoundException(
                    "Doctor does not have specialization " + specialization);
        }
        return mapper.toDto(doctorRepository.save(doctor));
    }

    private Doctor getDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id " + id));
    }

    private MedicalUnit getMedicalUnitOrThrow(Long id) {
        return medicalUnitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medical unit not found with id " + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }
}
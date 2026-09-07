package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.patient.PatientRequestDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.exception.UserAlreadyPatientException;
import com.andrei.project.medicalplatform.mapper.PatientMapper;
import com.andrei.project.medicalplatform.model.Patient;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import com.andrei.project.medicalplatform.repository.spec.PatientSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;

    public PatientResponseDto create(PatientRequestDto request) {
        User user = getUserOrThrow(request.userId());

        if (patientRepository.existsByUser_Id(user.getId())) {
            throw new UserAlreadyPatientException(
                    "User with id " + user.getId() + " is already registered as a patient");
        }

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setBloodType(request.bloodType());

        return mapper.toDto(patientRepository.save(patient));
    }

    public PatientResponseDto update(Long id, PatientRequestDto request) {
        Patient patient = getPatientOrThrow(id);

        if (!patient.getUser().getId().equals(request.userId())) {
            if (patientRepository.existsByUser_Id(request.userId())) {
                throw new UserAlreadyPatientException(
                        "User with id " + request.userId() + " is already registered as a patient");
            }
            patient.setUser(getUserOrThrow(request.userId()));
        }

        patient.setDateOfBirth(request.dateOfBirth());
        patient.setBloodType(request.bloodType());

        return mapper.toDto(patientRepository.save(patient));
    }

    public void delete(Long id) {
        patientRepository.delete(getPatientOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PatientResponseDto getById(Long id) {
        return mapper.toDto(getPatientOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDto> getAll(String firstName, String lastName, Pageable pageable) {
        return patientRepository
                .findAll(PatientSpecifications.filterBy(firstName, lastName), pageable)
                .map(mapper::toDto);
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id " + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }
}
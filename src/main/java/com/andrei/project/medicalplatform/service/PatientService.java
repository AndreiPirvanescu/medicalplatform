package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.patient.PatientRegistrationDTO;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDTO;
import com.andrei.project.medicalplatform.exception.EmailAlreadyExistsException;
import com.andrei.project.medicalplatform.mapper.PatientToPatientResponseDTOMapper;
import com.andrei.project.medicalplatform.model.Patient;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientToPatientResponseDTOMapper patientToPatientResponseDTOMapper;
    // private final PasswordEncoder passwordEncoder;

    @Transactional
    public PatientResponseDTO registerPatient(PatientRegistrationDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Un utilizator cu acest email există deja.");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // TODO: passwordEncoder.encode(dto.getPassword())
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
//        user.setRole(Role.PATIENT);
        User savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(savedUser);
//        patient.setPhoneNumber(dto.getPhoneNumber());
//        patient.setBirthDate(dto.getBirthDate());
        patient.setBloodType(dto.getBloodType());

        Patient savedPatient = patientRepository.save(patient);
        return patientToPatientResponseDTOMapper.mapToResponseDto(savedPatient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientToPatientResponseDTOMapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pacientul cu ID-ul " + id + " nu a fost găsit."));
        return patientToPatientResponseDTOMapper.mapToResponseDto(patient);
    }

    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientRegistrationDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pacientul cu ID-ul " + id + " nu a fost găsit."));

        User user = patient.getUser();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        userRepository.save(user);

//        patient.setPhoneNumber(dto.getPhoneNumber());
//        patient.setBirthDate(dto.getBirthDate());
        patient.setBloodType(dto.getBloodType());

        Patient updatedPatient = patientRepository.save(patient);
        return patientToPatientResponseDTOMapper.mapToResponseDto(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pacientul cu ID-ul " + id + " nu a fost găsit."));

        patientRepository.delete(patient);
        userRepository.delete(patient.getUser());
    }
}
package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.doctor.DoctorCreateRequestDTO;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDTO;
import com.andrei.project.medicalplatform.exception.EmailAlreadyExistsException;
import com.andrei.project.medicalplatform.mapper.DoctorToDoctorResponseDTOMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Role;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorToDoctorResponseDTOMapper doctorToDoctorResponseDTOMapper;
    // private final PasswordEncoder passwordEncoder; // Decomentează când adaugi Spring Security

    @Transactional
    public DoctorResponseDTO addDoctor(DoctorCreateRequestDTO doctorDto) {
        if (userRepository.existsByEmail(doctorDto.email())) {
            throw new EmailAlreadyExistsException("Un utilizator cu acest email există deja.");
        }

        User user = new User();
        user.setEmail(doctorDto.email());
        user.setPassword(doctorDto.password()); // TODO: Aici ar trebui passwordEncoder.encode(dto.getPassword())
        user.setFirstName(doctorDto.firstName());
        user.setLastName(doctorDto.lastName());
        user.setRole(Role.DOCTOR);
        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecializations(doctorDto.specializations());
        doctor.setLicenseNumber(doctorDto.licenseNumber());
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorToDoctorResponseDTOMapper.mapToResponseDto(savedDoctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctorToDoctorResponseDTOMapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No doctor could be found with id: " + id));
        return doctorToDoctorResponseDTOMapper.mapToResponseDto(doctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Long id, DoctorCreateRequestDTO doctorCreateRequestDto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctorul cu ID-ul " + id + " nu a fost găsit."));

        // Actualizare date User asociat
        User user = doctor.getUser();
        user.setFirstName(doctorCreateRequestDto.firstName());
        user.setLastName(doctorCreateRequestDto.lastName());
        // Email-ul și parola pot fi lăsate neschimbate sau actualizate cu validări suplimentare
        userRepository.save(user);

        // Actualizare date specifice Doctor
        doctor.setSpecializations(doctorCreateRequestDto.specializations());
        doctor.setLicenseNumber(doctorCreateRequestDto.licenseNumber());
        Doctor updatedDoctor = doctorRepository.save(doctor);

        return doctorToDoctorResponseDTOMapper.mapToResponseDto(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctorul cu ID-ul " + id + " nu a fost găsit."));

        // Ștergem doctorul și utilizatorul asociat (Cascade-ul manual sau din DB se ocupă de restul)
        doctorRepository.delete(doctor);
        userRepository.delete(doctor.getUser());
    }
}
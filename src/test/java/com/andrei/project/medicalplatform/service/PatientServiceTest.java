package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.patient.PatientRequestDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.exception.UserAlreadyPatientException;
import com.andrei.project.medicalplatform.mapper.PatientMapper;
import com.andrei.project.medicalplatform.model.Patient;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: PatientRequestDto(Long userId, LocalDate dateOfBirth, String bloodType) is a
 * record; if bloodType is actually a dedicated enum in your codebase, swap the String literals
 * below for the real enum constants. Patient/User are standard JPA entities with a no-arg
 * constructor and getters/setters mirroring the ones referenced in PatientService.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientMapper mapper;

    @InjectMocks
    private PatientService patientService;

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    // ---------- create ----------

    @Test
    void create_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.create(new PatientRequestDto(1L, LocalDate.of(1990, 1, 1), "O+")))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(patientRepository, mapper);
    }

    @Test
    void create_throws_whenUserAlreadyPatient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(patientRepository.existsByUser_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(new PatientRequestDto(1L, LocalDate.of(1990, 1, 1), "O+")))
                .isInstanceOf(UserAlreadyPatientException.class);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void create_savesPatient_whenUserExistsAndNotAlreadyPatient() {
        User user = userWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUser_Id(1L)).thenReturn(false);
        Patient saved = new Patient();
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);
        PatientResponseDto responseDto = mock(PatientResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        LocalDate dob = LocalDate.of(1990, 1, 1);
        PatientResponseDto result = patientService.create(new PatientRequestDto(1L, dob, "O+"));

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(result).isSameAs(responseDto);
    }

    // ---------- update ----------

    @Test
    void update_throws_whenPatientNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.update(1L, new PatientRequestDto(1L, LocalDate.now(), "A+")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_skipsUserCheck_whenUserIdUnchanged() {
        User user = userWithId(1L);
        Patient patient = new Patient();
        patient.setUser(user);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        PatientResponseDto responseDto = mock(PatientResponseDto.class);
        when(mapper.toDto(patient)).thenReturn(responseDto);

        LocalDate dob = LocalDate.of(1985, 5, 5);
        PatientResponseDto result = patientService.update(1L, new PatientRequestDto(1L, dob, "B-"));

        verify(patientRepository, never()).existsByUser_Id(anyLong());
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_throws_whenNewUserAlreadyPatient() {
        Patient patient = new Patient();
        patient.setUser(userWithId(1L));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByUser_Id(2L)).thenReturn(true);

        assertThatThrownBy(() -> patientService.update(1L, new PatientRequestDto(2L, LocalDate.now(), "A+")))
                .isInstanceOf(UserAlreadyPatientException.class);
        verify(userRepository, never()).findById(2L);
    }

    @Test
    void update_setsNewUser_whenChangedAndNotAlreadyPatient() {
        Patient patient = new Patient();
        patient.setUser(userWithId(1L));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.existsByUser_Id(2L)).thenReturn(false);
        User newUser = userWithId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(patientRepository.save(patient)).thenReturn(patient);
        PatientResponseDto responseDto = mock(PatientResponseDto.class);
        when(mapper.toDto(patient)).thenReturn(responseDto);

        patientService.update(1L, new PatientRequestDto(2L, LocalDate.now(), "AB+"));

        assertThat(patient.getUser()).isEqualTo(newUser);
    }

    // ---------- delete ----------

    @Test
    void delete_throws_whenNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> patientService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesPatient_whenFound() {
        Patient patient = new Patient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.delete(1L);

        verify(patientRepository).delete(patient);
    }

    // ---------- getById ----------

    @Test
    void getById_throws_whenNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> patientService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        Patient patient = new Patient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        PatientResponseDto dto = mock(PatientResponseDto.class);
        when(mapper.toDto(patient)).thenReturn(dto);

        assertThat(patientService.getById(1L)).isSameAs(dto);
    }

    // ---------- getAll ----------

    @Test
    @SuppressWarnings("unchecked")
    void getAll_mapsPageOfResults() {
        Patient patient = new Patient();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Patient> page = new PageImpl<>(List.of(patient));
        when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        PatientResponseDto dto = mock(PatientResponseDto.class);
        when(mapper.toDto(patient)).thenReturn(dto);

        Page<PatientResponseDto> result = patientService.getAll("Jane", "Doe", pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }
}

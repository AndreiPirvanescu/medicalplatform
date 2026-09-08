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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private MedicalUnitRepository medicalUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DoctorMapper mapper;

    @InjectMocks
    private DoctorService doctorService;

    private static final Long UNIT_ID = 1L;
    private static final Specialization SPEC_A = Specialization.CARDIOLOGIE;
    private static final Specialization SPEC_B = Specialization.CHIRURGIE;

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private MedicalUnit unitWithId(Long id) {
        MedicalUnit u = new MedicalUnit();
        u.setId(id);
        return u;
    }


    @Test
    void create_throws_whenMedicalUnitNotFound() {
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(UNIT_ID,
                new DoctorRequestDto(1L, "LIC-1", List.of(SPEC_A))))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_throws_whenUserNotFound() {
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithId(UNIT_ID)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(UNIT_ID,
                new DoctorRequestDto(1L, "LIC-1", List.of(SPEC_A))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_throws_whenUserAlreadyDoctor() {
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithId(UNIT_ID)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(doctorRepository.existsByUser_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(UNIT_ID,
                new DoctorRequestDto(1L, "LIC-1", List.of(SPEC_A))))
                .isInstanceOf(UserAlreadyDoctorException.class);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_savesDoctor_whenValid() {
        MedicalUnit unit = unitWithId(UNIT_ID);
        User user = userWithId(1L);
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUser_Id(1L)).thenReturn(false);
        Doctor saved = new Doctor();
        when(doctorRepository.save(any(Doctor.class))).thenReturn(saved);
        DoctorResponseDto responseDto = mock(DoctorResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        DoctorResponseDto result = doctorService.create(UNIT_ID,
                new DoctorRequestDto(1L, "LIC-1", List.of(SPEC_A)));

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor persisted = captor.getValue();
        assertThat(persisted.getMedicalUnit()).isEqualTo(unit);
        assertThat(persisted.getUser()).isEqualTo(user);
        assertThat(persisted.getLicenseNumber()).isEqualTo("LIC-1");
        assertThat(persisted.getSpecializations()).containsExactly(SPEC_A);
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.update(1L, new DoctorRequestDto(1L, "LIC-1", List.of())))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_skipsUserCheck_whenUserIdUnchanged() {
        Doctor doctor = new Doctor();
        doctor.setUser(userWithId(1L));
        doctor.setSpecializations(new ArrayList<>());
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        DoctorResponseDto responseDto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(responseDto);

        DoctorResponseDto result = doctorService.update(1L, new DoctorRequestDto(1L, "LIC-2", List.of(SPEC_B)));

        verify(doctorRepository, never()).existsByUser_Id(anyLong());
        assertThat(doctor.getLicenseNumber()).isEqualTo("LIC-2");
        assertThat(doctor.getSpecializations()).containsExactly(SPEC_B);
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_throws_whenNewUserAlreadyDoctor() {
        Doctor doctor = new Doctor();
        doctor.setUser(userWithId(1L));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.existsByUser_Id(2L)).thenReturn(true);

        assertThatThrownBy(() -> doctorService.update(1L, new DoctorRequestDto(2L, "LIC-1", List.of())))
                .isInstanceOf(UserAlreadyDoctorException.class);
    }

    @Test
    void update_setsNewUser_whenChangedAndAvailable() {
        Doctor doctor = new Doctor();
        doctor.setUser(userWithId(1L));
        doctor.setSpecializations(new ArrayList<>());
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.existsByUser_Id(2L)).thenReturn(false);
        User newUser = userWithId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(mapper.toDto(doctor)).thenReturn(mock(DoctorResponseDto.class));

        doctorService.update(1L, new DoctorRequestDto(2L, "LIC-3", List.of()));

        assertThat(doctor.getUser()).isEqualTo(newUser);
    }


    @Test
    void delete_throws_whenNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesDoctor_whenFound() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.delete(1L);

        verify(doctorRepository).delete(doctor);
    }


    @Test
    void getById_throws_whenNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        assertThat(doctorService.getById(1L)).isSameAs(dto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAll_mapsPageOfResults() {
        Doctor doctor = new Doctor();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        Page<DoctorResponseDto> result = doctorService.getAll("Ann", SPEC_A, UNIT_ID, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }


    @Test
    void getByMedicalUnit_throws_whenUnitNotFound() {
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorService.getByMedicalUnit(UNIT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByMedicalUnit_returnsMappedList() {
        when(medicalUnitRepository.findById(UNIT_ID)).thenReturn(Optional.of(unitWithId(UNIT_ID)));
        Doctor doctor = new Doctor();
        when(doctorRepository.findByMedicalUnit_Id(UNIT_ID)).thenReturn(List.of(doctor));
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        assertThat(doctorService.getByMedicalUnit(UNIT_ID)).containsExactly(dto);
    }

    @Test
    void addSpecialization_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorService.addSpecialization(1L, SPEC_A))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addSpecialization_throws_whenAlreadyPresent() {
        Doctor doctor = new Doctor();
        doctor.setSpecializations(new ArrayList<>(List.of(SPEC_A)));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.addSpecialization(1L, SPEC_A))
                .isInstanceOf(SpecializationAlreadyExistsException.class);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addSpecialization_addsAndSaves_whenNotPresent() {
        Doctor doctor = new Doctor();
        doctor.setSpecializations(new ArrayList<>());
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        DoctorResponseDto result = doctorService.addSpecialization(1L, SPEC_A);

        assertThat(doctor.getSpecializations()).containsExactly(SPEC_A);
        assertThat(result).isSameAs(dto);
    }

    @Test
    void removeSpecialization_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorService.removeSpecialization(1L, SPEC_A))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeSpecialization_throws_whenNotPresent() {
        Doctor doctor = new Doctor();
        doctor.setSpecializations(new ArrayList<>());
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.removeSpecialization(1L, SPEC_A))
                .isInstanceOf(EntityNotFoundException.class);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void removeSpecialization_removesAndSaves_whenPresent() {
        Doctor doctor = new Doctor();
        doctor.setSpecializations(new ArrayList<>(List.of(SPEC_A)));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        DoctorResponseDto dto = mock(DoctorResponseDto.class);
        when(mapper.toDto(doctor)).thenReturn(dto);

        DoctorResponseDto result = doctorService.removeSpecialization(1L, SPEC_A);

        assertThat(doctor.getSpecializations()).isEmpty();
        assertThat(result).isSameAs(dto);
    }
}

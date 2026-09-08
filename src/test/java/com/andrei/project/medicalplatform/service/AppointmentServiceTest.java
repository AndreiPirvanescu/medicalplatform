package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentRequestDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentUpdateDto;
import com.andrei.project.medicalplatform.exception.InvalidAppointmentStatusException;
import com.andrei.project.medicalplatform.exception.TimeSlotUnavailableException;
import com.andrei.project.medicalplatform.mapper.AppointmentMapper;
import com.andrei.project.medicalplatform.model.Appointment;
import com.andrei.project.medicalplatform.model.AppointmentStatus;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Patient;
import com.andrei.project.medicalplatform.repository.AppointmentRepository;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: AppointmentRequestDto(LocalDateTime appointmentDateTime) and
 * AppointmentUpdateDto(AppointmentStatus status, LocalDateTime appointmentDateTime) are records
 * whose accessors match the calls used inside AppointmentService. AppointmentStatus enum
 * constants SCHEDULED/COMPLETED/CANCELLED are taken directly from the production source.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentMapper mapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private static final Long PATIENT_ID = 1L;
    private static final Long DOCTOR_ID = 2L;
    private final LocalDateTime dateTime = LocalDateTime.of(2026, 5, 1, 10, 0);

    private Patient patientWithId(Long id) {
        Patient p = new Patient();
        p.setId(id);
        return p;
    }

    private Doctor doctorWithId(Long id) {
        Doctor d = new Doctor();
        d.setId(id);
        return d;
    }

    // ---------- create ----------

    @Test
    void create_throws_whenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(PATIENT_ID, DOCTOR_ID, new AppointmentRequestDto(dateTime)))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void create_throws_whenDoctorNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(PATIENT_ID, DOCTOR_ID, new AppointmentRequestDto(dateTime)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_throws_whenSlotAlreadyTaken() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        when(appointmentRepository.existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
                DOCTOR_ID, dateTime, AppointmentStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(PATIENT_ID, DOCTOR_ID, new AppointmentRequestDto(dateTime)))
                .isInstanceOf(TimeSlotUnavailableException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_savesAppointment_asScheduled_whenSlotAvailable() {
        Patient patient = patientWithId(PATIENT_ID);
        Doctor doctor = doctorWithId(DOCTOR_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
                DOCTOR_ID, dateTime, AppointmentStatus.CANCELLED)).thenReturn(false);
        Appointment saved = new Appointment();
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
        AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        AppointmentResponseDto result = appointmentService.create(PATIENT_ID, DOCTOR_ID, new AppointmentRequestDto(dateTime));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        Appointment persisted = captor.getValue();
        assertThat(persisted.getPatient()).isEqualTo(patient);
        assertThat(persisted.getDoctor()).isEqualTo(doctor);
        assertThat(persisted.getAppointmentDateTime()).isEqualTo(dateTime);
        assertThat(persisted.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result).isSameAs(responseDto);
    }

    // ---------- update ----------

    @Test
    void update_throws_whenAppointmentNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.update(1L,
                new AppointmentUpdateDto(dateTime, AppointmentStatus.COMPLETED)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_throws_whenTargetStatusIsScheduled() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dateTime);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.update(1L,
                new AppointmentUpdateDto(dateTime, AppointmentStatus.SCHEDULED)))
                .isInstanceOf(InvalidAppointmentStatusException.class);
    }

    @Test
    void update_skipsSlotCheck_whenDateTimeUnchanged() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dateTime);
        appointment.setDoctor(doctorWithId(DOCTOR_ID));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(appointment)).thenReturn(responseDto);

        AppointmentResponseDto result = appointmentService.update(1L,
                new AppointmentUpdateDto(dateTime, AppointmentStatus.CANCELLED));

        verify(appointmentRepository, never())
                .existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(any(), any(), any());
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_throws_whenNewSlotUnavailable() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dateTime);
        appointment.setDoctor(doctorWithId(DOCTOR_ID));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        LocalDateTime newTime = dateTime.plusHours(1);
        when(appointmentRepository.existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
                DOCTOR_ID, newTime, AppointmentStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.update(1L,
                new AppointmentUpdateDto(newTime, AppointmentStatus.COMPLETED)))
                .isInstanceOf(TimeSlotUnavailableException.class);
    }

    @Test
    void update_updatesDateTimeAndStatus_whenNewSlotAvailable() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(dateTime);
        appointment.setDoctor(doctorWithId(DOCTOR_ID));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        LocalDateTime newTime = dateTime.plusHours(1);
        when(appointmentRepository.existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
                DOCTOR_ID, newTime, AppointmentStatus.CANCELLED)).thenReturn(false);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(mapper.toDto(appointment)).thenReturn(mock(AppointmentResponseDto.class));

        appointmentService.update(1L, new AppointmentUpdateDto(newTime, AppointmentStatus.COMPLETED));

        assertThat(appointment.getAppointmentDateTime()).isEqualTo(newTime);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    // ---------- delete ----------

    @Test
    void delete_throws_whenNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesAppointment_whenFound() {
        Appointment appointment = new Appointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        appointmentService.delete(1L);

        verify(appointmentRepository).delete(appointment);
    }

    // ---------- getById ----------

    @Test
    void getById_throws_whenNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        Appointment appointment = new Appointment();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        AppointmentResponseDto dto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(appointment)).thenReturn(dto);

        assertThat(appointmentService.getById(1L)).isSameAs(dto);
    }

    // ---------- getAll ----------

    @Test
    @SuppressWarnings("unchecked")
    void getAll_mapsPageOfResults() {
        Appointment appointment = new Appointment();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Appointment> page = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        AppointmentResponseDto dto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(appointment)).thenReturn(dto);

        Page<AppointmentResponseDto> result = appointmentService.getAll(
                PATIENT_ID, DOCTOR_ID, AppointmentStatus.SCHEDULED, null, null, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    // ---------- getByDoctor ----------

    @Test
    void getByDoctor_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.getByDoctor(DOCTOR_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByDoctor_returnsMappedList() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        Appointment appointment = new Appointment();
        when(appointmentRepository.findByDoctor_Id(DOCTOR_ID)).thenReturn(List.of(appointment));
        AppointmentResponseDto dto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(appointment)).thenReturn(dto);

        assertThat(appointmentService.getByDoctor(DOCTOR_ID)).containsExactly(dto);
    }

    // ---------- getByPatient ----------

    @Test
    void getByPatient_throws_whenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.getByPatient(PATIENT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByPatient_returnsMappedList() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        Appointment appointment = new Appointment();
        when(appointmentRepository.findByPatient_Id(PATIENT_ID)).thenReturn(List.of(appointment));
        AppointmentResponseDto dto = mock(AppointmentResponseDto.class);
        when(mapper.toDto(appointment)).thenReturn(dto);

        assertThat(appointmentService.getByPatient(PATIENT_ID)).containsExactly(dto);
    }
}

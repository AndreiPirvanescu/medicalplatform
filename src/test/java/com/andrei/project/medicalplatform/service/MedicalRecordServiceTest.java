package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.medicalrecord.*;
import com.andrei.project.medicalplatform.exception.MedicalRecordAlreadyExistsException;
import com.andrei.project.medicalplatform.mapper.MedicalRecordMapper;
import com.andrei.project.medicalplatform.model.*;
import com.andrei.project.medicalplatform.repository.AppointmentRepository;
import com.andrei.project.medicalplatform.repository.MedicalRecordRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.PrescriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: MedicalRecordRequestDto(String notes) and MedicalRecordUpdateDto(String notes)
 * are records. MedicalHistoryEntryDto's canonical constructor order is copied verbatim from
 * the constructor calls inside MedicalRecordService (type, date, doctorId, doctorName,
 * description), and its ".date()" accessor is used directly in the source's sort comparator, so
 * that accessor name is confirmed rather than guessed.
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private MedicalRecordMapper mapper;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private static final Long PATIENT_ID = 1L;

    private Patient patientWithId(Long id) {
        Patient p = new Patient();
        p.setId(id);
        return p;
    }

    private Doctor doctorWithUser(Long id, String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setUser(user);
        return doctor;
    }

    // ---------- create ----------

    @Test
    void create_throws_whenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.create(PATIENT_ID, new MedicalRecordRequestDto("notes")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_throws_whenRecordAlreadyExists() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        when(medicalRecordRepository.existsByPatient_Id(PATIENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> medicalRecordService.create(PATIENT_ID, new MedicalRecordRequestDto("notes")))
                .isInstanceOf(MedicalRecordAlreadyExistsException.class);
        verify(medicalRecordRepository, never()).save(any());
    }

    @Test
    void create_savesRecord_whenNoneExists() {
        Patient patient = patientWithId(PATIENT_ID);
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(medicalRecordRepository.existsByPatient_Id(PATIENT_ID)).thenReturn(false);
        MedicalRecord saved = new MedicalRecord();
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(saved);
        MedicalRecordResponseDto responseDto = mock(MedicalRecordResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        MedicalRecordResponseDto result = medicalRecordService.create(PATIENT_ID, new MedicalRecordRequestDto("notes"));

        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getPatient()).isEqualTo(patient);
        assertThat(captor.getValue().getNotes()).isEqualTo("notes");
        assertThat(captor.getValue().getCreatedDate()).isNotNull();
        assertThat(result).isSameAs(responseDto);
    }

    // ---------- update ----------

    @Test
    void update_throws_whenNotFound() {
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.update(1L, new MedicalRecordUpdateDto("new notes")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_updatesNotes_whenFound() {
        MedicalRecord record = new MedicalRecord();
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);
        MedicalRecordResponseDto responseDto = mock(MedicalRecordResponseDto.class);
        when(mapper.toDto(record)).thenReturn(responseDto);

        MedicalRecordResponseDto result = medicalRecordService.update(1L, new MedicalRecordUpdateDto("new notes"));

        assertThat(record.getNotes()).isEqualTo("new notes");
        assertThat(result).isSameAs(responseDto);
    }

    // ---------- getById ----------

    @Test
    void getById_throws_whenNotFound() {
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalRecordService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        MedicalRecord record = new MedicalRecord();
        when(medicalRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        MedicalRecordResponseDto dto = mock(MedicalRecordResponseDto.class);
        when(mapper.toDto(record)).thenReturn(dto);

        assertThat(medicalRecordService.getById(1L)).isSameAs(dto);
    }

    // ---------- getByPatient ----------

    @Test
    void getByPatient_throws_whenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalRecordService.getByPatient(PATIENT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByPatient_throws_whenNoRecordExists() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        when(medicalRecordRepository.findByPatient_Id(PATIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.getByPatient(PATIENT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByPatient_returnsMappedDto_whenFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        MedicalRecord record = new MedicalRecord();
        when(medicalRecordRepository.findByPatient_Id(PATIENT_ID)).thenReturn(Optional.of(record));
        MedicalRecordResponseDto dto = mock(MedicalRecordResponseDto.class);
        when(mapper.toDto(record)).thenReturn(dto);

        assertThat(medicalRecordService.getByPatient(PATIENT_ID)).isSameAs(dto);
    }

    // ---------- getHistory ----------

    @Test
    void getHistory_throws_whenPatientNotFound() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalRecordService.getHistory(PATIENT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getHistory_onlyIncludesCompletedAppointments_andMergesWithTreatments_sortedDescending() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));

        Doctor doctor = doctorWithUser(2L, "Ann", "Lee");

        Appointment completed = new Appointment();
        completed.setStatus(AppointmentStatus.COMPLETED);
        completed.setAppointmentDateTime(LocalDateTime.of(2026, 2, 1, 9, 0));
        completed.setDoctor(doctor);

        Appointment scheduled = new Appointment();
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        scheduled.setAppointmentDateTime(LocalDateTime.of(2026, 3, 1, 9, 0));
        scheduled.setDoctor(doctor);

        when(appointmentRepository.findByPatient_Id(PATIENT_ID)).thenReturn(List.of(completed, scheduled));

        Medication med = new Medication();
        med.setName("Aspirin");
        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor);
        prescription.setIssueDate(LocalDateTime.of(2026, 1, 1, 9, 0));
        prescription.setMedications(new ArrayList<>(List.of(med)));
        when(prescriptionRepository.findByPatient_Id(PATIENT_ID)).thenReturn(List.of(prescription));

        List<MedicalHistoryEntryDto> result = medicalRecordService.getHistory(PATIENT_ID);

        assertThat(result).containsExactly(
                new MedicalHistoryEntryDto(HistoryEntryType.CONSULTATION, completed.getAppointmentDateTime(),
                        doctor.getId(), "Ann Lee", "Consultation completed"),
                new MedicalHistoryEntryDto(HistoryEntryType.TREATMENT, prescription.getIssueDate(),
                        doctor.getId(), "Ann Lee", "Aspirin"));
    }

    @Test
    void getHistory_usesPlaceholder_whenPrescriptionHasNoMedications() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patientWithId(PATIENT_ID)));
        when(appointmentRepository.findByPatient_Id(PATIENT_ID)).thenReturn(List.of());

        Doctor doctor = doctorWithUser(2L, "Ann", "Lee");
        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor);
        prescription.setIssueDate(LocalDateTime.of(2026, 1, 1, 9, 0));
        prescription.setMedications(new ArrayList<>());
        when(prescriptionRepository.findByPatient_Id(PATIENT_ID)).thenReturn(List.of(prescription));

        List<MedicalHistoryEntryDto> result = medicalRecordService.getHistory(PATIENT_ID);

        assertThat(result).containsExactly(
                new MedicalHistoryEntryDto(HistoryEntryType.TREATMENT, prescription.getIssueDate(),
                        doctor.getId(), "Ann Lee", "No medications listed"));
    }
}

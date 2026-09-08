package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.medication.MedicationHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.exception.MedicationAlreadyInPrescriptionException;
import com.andrei.project.medicalplatform.exception.MedicationNotInPrescriptionException;
import com.andrei.project.medicalplatform.mapper.MedicationMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Medication;
import com.andrei.project.medicalplatform.model.Prescription;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.MedicationRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.PrescriptionRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: Medication/Prescription/Doctor/User entities have getters/setters mirroring
 * those used in MedicationService. MedicationHistoryEntryDto's canonical constructor order is
 * copied verbatim from the constructor call in MedicationService, so equality assertions don't
 * depend on guessed accessor names.
 */
@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedicationMapper mapper;

    @InjectMocks
    private MedicationService medicationService;

    private Medication medication(Long id, String name, String dosage) {
        Medication m = new Medication();
        m.setId(id);
        m.setName(name);
        m.setDosage(dosage);
        return m;
    }

    private Prescription prescriptionWithMedications(Long id, Doctor doctor, LocalDateTime issueDate,
                                                       String notes, Medication... meds) {
        Prescription p = new Prescription();
        p.setId(id);
        p.setDoctor(doctor);
        p.setIssueDate(issueDate);
        p.setNotes(notes);
        p.setMedications(new ArrayList<>(List.of(meds)));
        return p;
    }

    private Doctor doctorWithUser(Long doctorId, String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setUser(user);
        return doctor;
    }

    // ---------- addToPrescription ----------

    @Test
    void addToPrescription_throws_whenPrescriptionNotFound() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.addToPrescription(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addToPrescription_throws_whenMedicationNotFound() {
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>());
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.addToPrescription(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addToPrescription_throws_whenAlreadyAttached() {
        Medication medication = medication(2L, "Ibuprofen", "200mg");
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>(List.of(medication)));
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication));

        assertThatThrownBy(() -> medicationService.addToPrescription(1L, 2L))
                .isInstanceOf(MedicationAlreadyInPrescriptionException.class);
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void addToPrescription_addsMedication_whenNotAlreadyAttached() {
        Medication medication = medication(2L, "Ibuprofen", "200mg");
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>());
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication));
        MedicationResponseDto dto = mock(MedicationResponseDto.class);
        when(mapper.toDto(medication)).thenReturn(dto);

        MedicationResponseDto result = medicationService.addToPrescription(1L, 2L);

        assertThat(prescription.getMedications()).containsExactly(medication);
        verify(prescriptionRepository).save(prescription);
        assertThat(result).isSameAs(dto);
    }

    // ---------- removeFromPrescription ----------

    @Test
    void removeFromPrescription_throws_whenPrescriptionNotFound() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicationService.removeFromPrescription(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeFromPrescription_throws_whenMedicationNotFound() {
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>());
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.removeFromPrescription(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeFromPrescription_throws_whenNotAttached() {
        Medication medication = medication(2L, "Ibuprofen", "200mg");
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>());
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication));

        assertThatThrownBy(() -> medicationService.removeFromPrescription(1L, 2L))
                .isInstanceOf(MedicationNotInPrescriptionException.class);
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void removeFromPrescription_removesMedication_whenAttached() {
        Medication medication = medication(2L, "Ibuprofen", "200mg");
        Prescription prescription = new Prescription();
        prescription.setMedications(new ArrayList<>(List.of(medication)));
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication));

        medicationService.removeFromPrescription(1L, 2L);

        assertThat(prescription.getMedications()).isEmpty();
        verify(prescriptionRepository).save(prescription);
    }

    // ---------- getAll ----------

    @Test
    @SuppressWarnings("unchecked")
    void getAll_mapsPageOfResults() {
        Medication medication = medication(1L, "Aspirin", "100mg");
        Pageable pageable = PageRequest.of(0, 5);
        Page<Medication> page = new PageImpl<>(List.of(medication));
        when(medicationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        MedicationResponseDto dto = mock(MedicationResponseDto.class);
        when(mapper.toDto(medication)).thenReturn(dto);

        Page<MedicationResponseDto> result = medicationService.getAll("Asp", null, pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    // ---------- getById ----------

    @Test
    void getById_throws_whenNotFound() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicationService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        Medication medication = medication(1L, "Aspirin", "100mg");
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
        MedicationResponseDto dto = mock(MedicationResponseDto.class);
        when(mapper.toDto(medication)).thenReturn(dto);

        assertThat(medicationService.getById(1L)).isSameAs(dto);
    }

    // ---------- getPatientHistory ----------

    @Test
    void getPatientHistory_throws_whenPatientNotFound() {
        when(patientRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> medicationService.getPatientHistory(1L, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPatientHistory_filtersByNameCaseInsensitive() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        Doctor doctor = doctorWithUser(5L, "Ann", "Lee");
        Medication aspirin = medication(1L, "Aspirin", "100mg");
        Medication ibuprofen = medication(2L, "Ibuprofen", "200mg");
        LocalDateTime issueDate = LocalDateTime.of(2026, 1, 1, 10, 0);
        Prescription prescription = prescriptionWithMedications(10L, doctor, issueDate, "some notes", aspirin, ibuprofen);
        when(prescriptionRepository.findByPatient_Id(1L)).thenReturn(List.of(prescription));

        List<MedicationHistoryEntryDto> result = medicationService.getPatientHistory(1L, "asp");

        assertThat(result).containsExactly(new MedicationHistoryEntryDto(
                aspirin.getId(), aspirin.getName(), aspirin.getDosage(),
                prescription.getId(), doctor.getId(), "Ann Lee",
                prescription.getIssueDate(), prescription.getNotes()));
    }

    @Test
    void getPatientHistory_sortsByIssueDateDescending() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        Doctor doctor = doctorWithUser(5L, "Ann", "Lee");
        Medication med1 = medication(1L, "Aspirin", "100mg");
        Medication med2 = medication(2L, "Paracetamol", "500mg");
        LocalDateTime older = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 1, 1, 10, 0);
        Prescription p1 = prescriptionWithMedications(10L, doctor, older, "n1", med1);
        Prescription p2 = prescriptionWithMedications(11L, doctor, newer, "n2", med2);
        when(prescriptionRepository.findByPatient_Id(1L)).thenReturn(List.of(p1, p2));

        List<MedicationHistoryEntryDto> result = medicationService.getPatientHistory(1L, null);

        assertThat(result).extracting(MedicationHistoryEntryDto::issueDate)
                .containsExactly(newer, older);
    }
}

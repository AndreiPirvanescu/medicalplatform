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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordMapper mapper;

    public MedicalRecordResponseDto create(Long patientId, MedicalRecordRequestDto request) {
        Patient patient = getPatientOrThrow(patientId);

        if (medicalRecordRepository.existsByPatient_Id(patientId)) {
            throw new MedicalRecordAlreadyExistsException(
                    "Patient with id " + patientId + " already has a medical record");
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setCreatedDate(LocalDateTime.now());
        record.setNotes(request.notes());

        return mapper.toDto(medicalRecordRepository.save(record));
    }

    public MedicalRecordResponseDto update(Long id, MedicalRecordUpdateDto request) {
        MedicalRecord record = getRecordOrThrow(id);
        record.setNotes(request.notes());
        return mapper.toDto(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponseDto getById(Long id) {
        return mapper.toDto(getRecordOrThrow(id));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponseDto getByPatient(Long patientId) {
        getPatientOrThrow(patientId);
        MedicalRecord record = medicalRecordRepository.findByPatient_Id(patientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No medical record found for patient with id " + patientId));
        return mapper.toDto(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalHistoryEntryDto> getHistory(Long patientId) {
        getPatientOrThrow(patientId);

        Stream<MedicalHistoryEntryDto> consultations = appointmentRepository
                .findByPatient_Id(patientId).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(this::toConsultationEntry);

        Stream<MedicalHistoryEntryDto> treatments = prescriptionRepository
                .findByPatient_Id(patientId).stream()
                .map(this::toTreatmentEntry);

        return Stream.concat(consultations, treatments)
                .sorted(Comparator.comparing(MedicalHistoryEntryDto::date).reversed())
                .toList();
    }

    private MedicalHistoryEntryDto toConsultationEntry(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        return new MedicalHistoryEntryDto(
                HistoryEntryType.CONSULTATION,
                appointment.getAppointmentDateTime(),
                doctor.getId(),
                doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
                "Consultation completed"
        );
    }

    private MedicalHistoryEntryDto toTreatmentEntry(Prescription prescription) {
        Doctor doctor = prescription.getDoctor();
        String medicationNames = prescription.getMedications().stream()
                .map(Medication::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No medications listed");

        return new MedicalHistoryEntryDto(
                HistoryEntryType.TREATMENT,
                prescription.getIssueDate(),
                doctor.getId(),
                doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
                medicationNames
        );
    }

    private MedicalRecord getRecordOrThrow(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medical record not found with id " + id));
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id " + id));
    }
}
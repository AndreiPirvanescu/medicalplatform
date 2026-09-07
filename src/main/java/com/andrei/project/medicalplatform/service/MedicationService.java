package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.medication.MedicationHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.exception.MedicationAlreadyInPrescriptionException;
import com.andrei.project.medicalplatform.exception.MedicationNotInPrescriptionException;
import com.andrei.project.medicalplatform.mapper.MedicationMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Medication;
import com.andrei.project.medicalplatform.model.Prescription;
import com.andrei.project.medicalplatform.repository.MedicationRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.PrescriptionRepository;
import com.andrei.project.medicalplatform.repository.spec.MedicationSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final MedicationMapper mapper;

    public MedicationResponseDto addToPrescription(Long prescriptionId, Long medicationId) {
        Prescription prescription = getPrescriptionOrThrow(prescriptionId);
        Medication medication = getMedicationOrThrow(medicationId);

        boolean alreadyPresent = prescription.getMedications().stream()
                .anyMatch(m -> m.getId().equals(medicationId));

        if (alreadyPresent) {
            throw new MedicationAlreadyInPrescriptionException(
                    "Medication with id " + medicationId +
                            " is already attached to prescription " + prescriptionId);
        }

        prescription.getMedications().add(medication);
        prescriptionRepository.save(prescription);

        return mapper.toDto(medication);
    }

    public void removeFromPrescription(Long prescriptionId, Long medicationId) {
        Prescription prescription = getPrescriptionOrThrow(prescriptionId);
        Medication medication = getMedicationOrThrow(medicationId);

        boolean removed = prescription.getMedications().removeIf(m -> m.getId().equals(medicationId));

        if (!removed) {
            throw new MedicationNotInPrescriptionException(
                    "Medication with id " + medicationId +
                            " is not attached to prescription " + prescriptionId);
        }

        prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public Page<MedicationResponseDto> getAll(String name, Long patientId, Pageable pageable) {
        return medicationRepository
                .findAll(MedicationSpecifications.filterBy(name, patientId), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public MedicationResponseDto getById(Long id) {
        return mapper.toDto(getMedicationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MedicationHistoryEntryDto> getPatientHistory(Long patientId, String name) {
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("Patient not found with id " + patientId);
        }

        return prescriptionRepository.findByPatient_Id(patientId).stream()
                .flatMap(prescription -> prescription.getMedications().stream()
                        .filter(medication -> matchesName(medication, name))
                        .map(medication -> toHistoryEntry(prescription, medication)))
                .sorted(Comparator.comparing(MedicationHistoryEntryDto::issueDate).reversed())
                .toList();
    }

    private boolean matchesName(Medication medication, String name) {
        return name == null || name.isBlank()
                || medication.getName().toLowerCase().contains(name.toLowerCase());
    }

    private MedicationHistoryEntryDto toHistoryEntry(Prescription prescription, Medication medication) {
        Doctor doctor = prescription.getDoctor();
        return new MedicationHistoryEntryDto(
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                prescription.getId(),
                doctor.getId(),
                doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
                prescription.getIssueDate(),
                prescription.getNotes()
        );
    }

    private Prescription getPrescriptionOrThrow(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id " + id));
    }

    private Medication getMedicationOrThrow(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medication not found with id " + id));
    }
}
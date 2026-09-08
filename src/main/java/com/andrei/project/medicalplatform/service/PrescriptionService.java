package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.prescription.PrescriptionRequestDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionUpdateDto;
import com.andrei.project.medicalplatform.exception.InvalidPrescriptionAppointmentException;
import com.andrei.project.medicalplatform.mapper.PrescriptionMapper;
import com.andrei.project.medicalplatform.model.*;
import com.andrei.project.medicalplatform.repository.*;
import com.andrei.project.medicalplatform.repository.spec.PrescriptionSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;
    private final PrescriptionMapper mapper;

    public PrescriptionResponseDto create(Long patientId, Long doctorId, PrescriptionRequestDto request) {
        Patient patient = getPatientOrThrow(patientId);
        Doctor doctor = getDoctorOrThrow(doctorId);
        Appointment appointment = resolveAppointment(request.appointmentId(), patientId, doctorId);
        List<Medication> medications = getMedicationsOrThrow(request.medicationIds());

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setAppointment(appointment);
        prescription.setIssueDate(LocalDateTime.now());
        prescription.setNotes(request.notes());
        prescription.setMedications(medications);

        return mapper.toDto(prescriptionRepository.save(prescription));
    }

    public PrescriptionResponseDto update(Long id, PrescriptionUpdateDto request) {
        Prescription prescription = getPrescriptionOrThrow(id);

        prescription.setNotes(request.notes());
        prescription.setMedications(getMedicationsOrThrow(request.medicationIds()));

        return mapper.toDto(prescriptionRepository.save(prescription));
    }

    public void delete(Long id) {
        prescriptionRepository.delete(getPrescriptionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PrescriptionResponseDto getById(Long id) {
        return mapper.toDto(getPrescriptionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponseDto> getAll(Long patientId, Long doctorId,
                                                LocalDateTime from, LocalDateTime to,
                                                Pageable pageable) {
        return prescriptionRepository
                .findAll(PrescriptionSpecifications.filterBy(patientId, doctorId, from, to), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getByPatient(Long patientId) {
        getPatientOrThrow(patientId);
        return prescriptionRepository.findByPatient_Id(patientId).stream()
                .map(mapper::toDto)
                .toList();
    }

    private Appointment resolveAppointment(Long appointmentId, Long patientId, Long doctorId) {
        if (appointmentId == null) {
            return null;
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id " + appointmentId));

        if (!appointment.getPatient().getId().equals(patientId)
                || !appointment.getDoctor().getId().equals(doctorId)) {
            throw new InvalidPrescriptionAppointmentException(
                    "Appointment with id " + appointmentId +
                            " does not belong to patient " + patientId + " and doctor " + doctorId);
        }

        return appointment;
    }

    private List<Medication> getMedicationsOrThrow(List<Long> medicationIds) {
        List<Medication> medications = medicationRepository.findAllById(medicationIds);

        if (medications.size() != medicationIds.size()) {
            throw new EntityNotFoundException("One or more medication ids were not found");
        }

        return medications;
    }

    private Prescription getPrescriptionOrThrow(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id " + id));
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id " + id));
    }

    private Doctor getDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id " + id));
    }
}
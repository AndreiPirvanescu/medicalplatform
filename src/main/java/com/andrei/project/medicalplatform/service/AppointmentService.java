package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentRequestDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentUpdateDto;
import com.andrei.project.medicalplatform.exception.InvalidAppointmentStatusException;
import com.andrei.project.medicalplatform.exception.TimeSlotUnavailableException;
import com.andrei.project.medicalplatform.mapper.AppointmentMapper;
import com.andrei.project.medicalplatform.model.*;
import com.andrei.project.medicalplatform.repository.AppointmentRepository;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.spec.AppointmentSpecifications;
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
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper mapper;

    public AppointmentResponseDto create(Long patientId, Long doctorId, AppointmentRequestDto request) {
        Patient patient = getPatientOrThrow(patientId);
        Doctor doctor = getDoctorOrThrow(doctorId);

        ensureSlotAvailable(doctor.getId(), request.appointmentDateTime());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(request.appointmentDateTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return mapper.toDto(appointmentRepository.save(appointment));
    }

    public AppointmentResponseDto update(Long id, AppointmentUpdateDto request) {
        Appointment appointment = getAppointmentOrThrow(id);

        if (request.status() == AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusException(
                    "An appointment can only be updated to COMPLETED or CANCELLED");
        }

        if (!appointment.getAppointmentDateTime().equals(request.appointmentDateTime())) {
            ensureSlotAvailable(appointment.getDoctor().getId(), request.appointmentDateTime());
            appointment.setAppointmentDateTime(request.appointmentDateTime());
        }

        appointment.setStatus(request.status());

        return mapper.toDto(appointmentRepository.save(appointment));
    }

    public void delete(Long id) {
        appointmentRepository.delete(getAppointmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(Long id) {
        return mapper.toDto(getAppointmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> getAll(Long patientId, Long doctorId, AppointmentStatus status,
                                               LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return appointmentRepository
                .findAll(AppointmentSpecifications.filterBy(patientId, doctorId, status, from, to), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getByDoctor(Long doctorId) {
        getDoctorOrThrow(doctorId);
        return appointmentRepository.findByDoctor_Id(doctorId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getByPatient(Long patientId) {
        getPatientOrThrow(patientId);
        return appointmentRepository.findByPatient_Id(patientId).stream()
                .map(mapper::toDto)
                .toList();
    }

    private void ensureSlotAvailable(Long doctorId, LocalDateTime dateTime) {
        boolean conflict = appointmentRepository.existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
                doctorId, dateTime, AppointmentStatus.CANCELLED);
        if (conflict) {
            throw new TimeSlotUnavailableException(
                    "Doctor is not available at " + dateTime);
        }
    }

    private Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id " + id));
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
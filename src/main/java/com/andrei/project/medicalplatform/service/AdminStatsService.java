package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.admin.AdminStatsDto;
import com.andrei.project.medicalplatform.repository.AppointmentRepository;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.MedicalUnitRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final MedicalUnitRepository medicalUnitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AdminStatsDto getStats() {
        return new AdminStatsDto(
                medicalUnitRepository.count(),
                doctorRepository.count(),
                patientRepository.count(),
                appointmentRepository.count(),
                prescriptionRepository.count()
        );
    }
}
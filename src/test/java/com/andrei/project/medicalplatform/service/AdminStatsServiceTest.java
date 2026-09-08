package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.admin.AdminStatsDto;
import com.andrei.project.medicalplatform.repository.AppointmentRepository;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.MedicalUnitRepository;
import com.andrei.project.medicalplatform.repository.PatientRepository;
import com.andrei.project.medicalplatform.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * ASSUMPTION: AdminStatsDto's canonical constructor order is copied verbatim from
 * AdminStatsService (medicalUnits, doctors, patients, appointments, prescriptions), so equality
 * assertions don't rely on guessed accessor names.
 */
@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private MedicalUnitRepository medicalUnitRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void getStats_returnsCountsFromEachRepository() {
        when(medicalUnitRepository.count()).thenReturn(3L);
        when(doctorRepository.count()).thenReturn(10L);
        when(patientRepository.count()).thenReturn(100L);
        when(appointmentRepository.count()).thenReturn(250L);
        when(prescriptionRepository.count()).thenReturn(80L);

        AdminStatsDto result = adminStatsService.getStats();

        assertThat(result).isEqualTo(new AdminStatsDto(3L, 10L, 100L, 250L, 80L));
    }
}

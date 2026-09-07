package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Appointment;
import com.andrei.project.medicalplatform.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    boolean existsByDoctor_IdAndAppointmentDateTimeAndStatusNot(
            Long doctorId, LocalDateTime appointmentDateTime, AppointmentStatus status);

    List<Appointment> findByDoctor_Id(Long doctorId);

    List<Appointment> findByPatient_Id(Long patientId);
}
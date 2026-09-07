package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Appointment;
import com.andrei.project.medicalplatform.model.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AppointmentSpecifications {

    private AppointmentSpecifications() {}

    public static Specification<Appointment> hasPatientId(Long patientId) {
        return (root, query, cb) -> patientId != null
                ? cb.equal(root.get("patient").get("id"), patientId)
                : cb.conjunction();
    }

    public static Specification<Appointment> hasDoctorId(Long doctorId) {
        return (root, query, cb) -> doctorId != null
                ? cb.equal(root.get("doctor").get("id"), doctorId)
                : cb.conjunction();
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> status != null
                ? cb.equal(root.get("status"), status)
                : cb.conjunction();
    }

    public static Specification<Appointment> dateFrom(LocalDateTime from) {
        return (root, query, cb) -> from != null
                ? cb.greaterThanOrEqualTo(root.get("appointmentDateTime"), from)
                : cb.conjunction();
    }

    public static Specification<Appointment> dateTo(LocalDateTime to) {
        return (root, query, cb) -> to != null
                ? cb.lessThanOrEqualTo(root.get("appointmentDateTime"), to)
                : cb.conjunction();
    }

    public static Specification<Appointment> filterBy(Long patientId, Long doctorId,
                                                      AppointmentStatus status,
                                                      LocalDateTime from, LocalDateTime to) {
        return Specification.where(hasPatientId(patientId))
                .and(hasDoctorId(doctorId))
                .and(hasStatus(status))
                .and(dateFrom(from))
                .and(dateTo(to));
    }
}
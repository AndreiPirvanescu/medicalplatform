package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Prescription;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PrescriptionSpecifications {

    private PrescriptionSpecifications() {}

    public static Specification<Prescription> hasPatientId(Long patientId) {
        return (root, query, cb) -> patientId != null
                ? cb.equal(root.get("patient").get("id"), patientId)
                : cb.conjunction();
    }

    public static Specification<Prescription> hasDoctorId(Long doctorId) {
        return (root, query, cb) -> doctorId != null
                ? cb.equal(root.get("doctor").get("id"), doctorId)
                : cb.conjunction();
    }

    public static Specification<Prescription> issuedFrom(LocalDateTime from) {
        return (root, query, cb) -> from != null
                ? cb.greaterThanOrEqualTo(root.get("issueDate"), from)
                : cb.conjunction();
    }

    public static Specification<Prescription> issuedTo(LocalDateTime to) {
        return (root, query, cb) -> to != null
                ? cb.lessThanOrEqualTo(root.get("issueDate"), to)
                : cb.conjunction();
    }

    public static Specification<Prescription> filterBy(Long patientId, Long doctorId,
                                                       LocalDateTime from, LocalDateTime to) {
        return Specification.where(hasPatientId(patientId))
                .and(hasDoctorId(doctorId))
                .and(issuedFrom(from))
                .and(issuedTo(to));
    }
}
package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Medication;
import com.andrei.project.medicalplatform.model.Prescription;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class MedicationSpecifications {

    private MedicationSpecifications() {}

    public static Specification<Medication> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Medication> hasPatientId(Long patientId) {
        return (root, query, cb) -> {
            if (patientId == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<Medication, Prescription> prescriptions = root.join("prescriptions");
            return cb.equal(prescriptions.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Medication> filterBy(String name, Long patientId) {
        return Specification.where(hasNameLike(name)).and(hasPatientId(patientId));
    }
}
package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Patient;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PatientSpecifications {

    private PatientSpecifications() {}

    public static Specification<Patient> hasFirstName(String firstName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(firstName)) {
                return cb.conjunction();
            }
            var user = root.join("user", JoinType.LEFT);
            return cb.like(cb.lower(user.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<Patient> hasLastName(String lastName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(lastName)) {
                return cb.conjunction();
            }
            var user = root.join("user", JoinType.LEFT);
            return cb.like(cb.lower(user.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    public static Specification<Patient> filterBy(String firstName, String lastName) {
        return Specification.where(hasFirstName(firstName)).and(hasLastName(lastName));
    }
}
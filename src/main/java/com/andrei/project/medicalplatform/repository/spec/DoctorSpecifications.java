package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DoctorSpecifications {

    private DoctorSpecifications() {}

    public static Specification<Doctor> hasName(String name) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(name)) {
                return cb.conjunction();
            }
            var user = root.join("user", JoinType.LEFT);
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(user.get("firstName")), pattern),
                    cb.like(cb.lower(user.get("lastName")), pattern)
            );
        };
    }

    public static Specification<Doctor> hasSpecialization(Specialization specialization) {
        return (root, query, cb) -> {
            if (specialization == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            var specializations = root.join("specializations", JoinType.LEFT);
            return cb.equal(specializations, specialization);
        };
    }

    public static Specification<Doctor> hasMedicalUnitId(Long medicalUnitId) {
        return (root, query, cb) -> medicalUnitId != null
                ? cb.equal(root.get("medicalUnit").get("id"), medicalUnitId)
                : cb.conjunction();
    }

    public static Specification<Doctor> filterBy(String name, Specialization specialization, Long medicalUnitId) {
        return Specification.where(hasName(name))
                .and(hasSpecialization(specialization))
                .and(hasMedicalUnitId(medicalUnitId));
    }
}
package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Schedule;
import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DoctorSpecifications {

    private DoctorSpecifications() {}

    public static Specification<Doctor> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("user").get("firstName")), pattern),
                    cb.like(cb.lower(root.get("user").get("lastName")), pattern),
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("user").get("firstName"), " "),
                            root.get("user").get("lastName"))), pattern)
            );
        };
    }

    public static Specification<Doctor> hasSpecialization(Specialization specialization) {
        return (root, query, cb) -> specialization != null
                ? cb.isMember(specialization, root.get("specializations"))
                : cb.conjunction();
    }

    public static Specification<Doctor> hasMedicalUnitId(Long medicalUnitId) {
        return (root, query, cb) -> medicalUnitId != null
                ? cb.equal(root.get("medicalUnit").get("id"), medicalUnitId)
                : cb.conjunction();
    }

    public static Specification<Doctor> isAvailable(Boolean available, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (available == null || !available) {
                return cb.conjunction();
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            var scheduleRoot = subquery.from(Schedule.class);
            subquery.select(scheduleRoot.get("id"));

            Predicate predicate = cb.and(
                    cb.equal(scheduleRoot.get("doctor"), root),
                    cb.isTrue(scheduleRoot.get("available"))
            );

            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(scheduleRoot.get("startTime"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(scheduleRoot.get("endTime"), to));
            }

            subquery.where(predicate);
            return cb.exists(subquery);
        };
    }

    public static Specification<Doctor> filterByShort(String name, Specialization specialization, Long medicalUnitId) {
        return Specification.where(hasName(name))
                .and(hasSpecialization(specialization))
                .and(hasMedicalUnitId(medicalUnitId));
    }

    public static Specification<Doctor> filterBy(String name, Specialization specialization,
                                                 Long medicalUnitId, Boolean available,
                                                 LocalDateTime from, LocalDateTime to) {
        return Specification.where(hasName(name))
                .and(hasSpecialization(specialization))
                .and(hasMedicalUnitId(medicalUnitId))
                .and(isAvailable(available, from, to));
    }
}
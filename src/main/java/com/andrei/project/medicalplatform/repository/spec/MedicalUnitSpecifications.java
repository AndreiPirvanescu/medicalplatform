package com.andrei.project.medicalplatform.repository.spec;

import com.andrei.project.medicalplatform.model.MedicalUnit;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class MedicalUnitSpecifications {

    private MedicalUnitSpecifications() {}

    public static Specification<MedicalUnit> hasName(String name) {
        return (root, query, cb) -> StringUtils.hasText(name)
                ? cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
                : cb.conjunction();
    }

    public static Specification<MedicalUnit> hasLocation(String location) {
        return (root, query, cb) -> StringUtils.hasText(location)
                ? cb.like(cb.lower(root.get("address")), "%" + location.toLowerCase() + "%")
                : cb.conjunction();
    }

    public static Specification<MedicalUnit> filterBy(String name, String location) {
        return Specification.where(hasName(name)).and(hasLocation(location));
    }
}
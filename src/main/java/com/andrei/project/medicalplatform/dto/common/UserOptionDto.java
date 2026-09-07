package com.andrei.project.medicalplatform.dto.common;

/**
 * Lightweight projection used to populate <select> dropdowns in Thymeleaf
 * forms (choosing a manager for a medical unit, or the underlying user
 * account when creating a doctor/patient profile).
 * Wire this up to however your User entity/service actually looks -
 * e.g. a JPQL projection: "select new ...UserOptionDto(u.id, u.firstName || ' ' || u.lastName, u.email) from User u"
 */
public record UserOptionDto(
        Long id,
        String fullName,
        String email
) {}

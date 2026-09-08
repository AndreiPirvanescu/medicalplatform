package com.andrei.project.medicalplatform.model;

/**
 * ASSUMPTION - if you already have a role enum (or a different mechanism,
 * like a Role entity/table) for Users, DELETE this file and point
 * UserService/UserResponseDto at your real one instead. This is only here
 * because nothing about your User model was shared with me, and
 * ADMIN/MANAGER/DOCTOR/PATIENT is the smallest set that explains the
 * eligibility rules already implied by the other nine features (someone
 * manages a unit, someone becomes a doctor, someone registers as a patient).
 */
public enum UserRole {
    ADMIN,
    MANAGER,
    DOCTOR,
    PATIENT
}

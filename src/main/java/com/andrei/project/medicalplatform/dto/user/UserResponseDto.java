package com.andrei.project.medicalplatform.dto.user;

import java.util.List;

/**
 * ASSUMPTION: I still don't have your actual User entity, only Role (a
 * many-to-many, name-based role table). So firstName/lastName/email/phone
 * below are still a guess - only "roles" is now grounded in what you shared.
 * Adjust the field access in UserService if User's real fields differ.
 */
public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        List<String> roles
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}

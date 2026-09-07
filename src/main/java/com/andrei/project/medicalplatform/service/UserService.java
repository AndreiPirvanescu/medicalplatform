package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.common.UserRoleOptions;
import com.andrei.project.medicalplatform.dto.user.UserResponseDto;
import com.andrei.project.medicalplatform.model.Role;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backs the "which user is this?" dropdowns/info boxes on the Medical Unit
 * (manager), Doctor and Patient create/edit pages.
 *
 * Matched against your real Role entity (id, name, @ManyToMany(mappedBy =
 * "roles") Set<User> users) - a User can hold several roles at once, so
 * "eligible for X" means "has a role named X among possibly others".
 *
 * WHY THE DROPDOWNS WERE EMPTY: findManagers()/findEligibleDoctorUsers()/
 * findEligiblePatientUsers() used to do a strict exact-match lookup
 * (UserRepository.findByRoles_Name("MANAGER") etc.). If your actual role
 * rows are named anything else - different case ("Manager"), a "ROLE_"
 * prefix ("ROLE_MANAGER"), or no user has been assigned that role yet -
 * that query legitimately returns zero rows, and the template ends up with
 * an empty <select>. Two changes fix that:
 *
 *   1. UserRepository.findByRoleNameIgnoreCase matches case-insensitively
 *      and with-or-without a "ROLE_" prefix, so small naming differences no
 *      longer produce a silent empty list.
 *   2. If that STILL comes back empty (no user actually has the role yet),
 *      findByRoleWithFallback() falls back to returning every user instead
 *      of nothing, and logs a warning. The form is never stuck with a blank
 *      dropdown, and usedFallback is exposed to the controller/template so
 *      they can show a "no users are tagged with this role yet" banner
 *      instead of pretending the list is normal.
 *
 * REMAINING ASSUMPTIONS (User itself still wasn't shared):
 *   - User exposes getFirstName()/getLastName()/getEmail()/getPhone() -
 *     adjust toResponseDto()/toOptionDtos() below if the real names differ.
 *   - User's collection field for the many-to-many is literally named
 *     "roles" (see the comment on UserRepository).
 *   - The three role-name constants below (MANAGER/DOCTOR/PATIENT) are the
 *     right base strings - change them if your data uses something else
 *     entirely (not just casing/prefix, which is already handled).
 *
 * NOT done here: excluding users who are already a doctor/patient/manager
 * elsewhere - see the same note in the previous version of this file. Give
 * me your Doctor/Patient/MedicalUnit repository method names and I'll add
 * that filtering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_DOCTOR = "DOCTOR";
    private static final String ROLE_PATIENT = "PATIENT";

    private final UserRepository userRepository;

    public UserResponseDto getById(Long id) {
        return toResponseDto(getUserOrThrow(id));
    }

    public UserRoleOptions findManagers() {
        return findByRoleWithFallback(ROLE_MANAGER);
    }

    public UserRoleOptions findEligibleDoctorUsers() {
        return findByRoleWithFallback(ROLE_DOCTOR);
    }

    public UserRoleOptions findEligiblePatientUsers() {
        return findByRoleWithFallback(ROLE_PATIENT);
    }

    private UserRoleOptions findByRoleWithFallback(String roleName) {
        List<User> matched = userRepository.findByRoleNameIgnoreCase(roleName);
        boolean usedFallback = false;

        if (matched.isEmpty()) {
            usedFallback = true;
            List<User> all = userRepository.findAll();
            log.warn("No users found with role '{}' (checked case-insensitively, with/without a ROLE_ " +
                            "prefix). Falling back to listing all {} user(s) so the dropdown isn't empty - " +
                            "assign that role to at least one user, or fix the role name, to stop seeing this.",
                    roleName, all.size());
            matched = all;
        }

        return new UserRoleOptions(toOptionDtos(matched), usedFallback);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    private List<UserOptionDto> toOptionDtos(List<User> users) {
        return users.stream()
                .map(u -> new UserOptionDto(u.getId(), u.getFirstName() + " " + u.getLastName(), u.getEmail()))
                .toList();
    }

    private UserResponseDto toResponseDto(User u) {
        List<String> roleNames = u.getRoles() == null
                ? List.of()
                : u.getRoles().stream().map(Role::getName).toList();
        return new UserResponseDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(), roleNames);
    }
}

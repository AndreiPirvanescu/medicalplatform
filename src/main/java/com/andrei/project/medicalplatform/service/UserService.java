package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.user.UserResponseDto;
import com.andrei.project.medicalplatform.model.Role;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the "which user is this?" dropdowns/info boxes on the Medical Unit
 * (manager), Doctor and Patient create pages, and the read-only "linked
 * account" boxes on the Doctor/Patient edit pages.
 *
 * Per your latest instructions, Manager/Doctor/Patient candidates are all
 * the SAME pool: users holding no role at all (UserRepository.
 * findUsersWithNoRoles(), JPQL "WHERE u.roles IS EMPTY"). This replaces the
 * earlier role-name-based lookup entirely (there used to be a
 * findByRoleNameIgnoreCase("DOCTOR"/"PATIENT") path with a "show everyone"
 * fallback - gone now, nothing here checks Role.name any more). The
 * UserRepository derived/role queries are left in place in case you still
 * want them for something else (an admin "users by role" screen, etc.) but
 * nothing in this class calls them any more.
 *
 * Confirmed against the real DoctorMapper/PatientMapper you shared: User
 * does expose getFirstName()/getLastName()/getEmail()/getPhone(), so that
 * assumption is no longer a guess. The one still open is the exact field
 * name of the roles collection on User (see UserRepository) - needed for
 * findUsersWithNoRoles()'s "u.roles IS EMPTY" to resolve.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getById(Long id) {
        return toResponseDto(getUserOrThrow(id));
    }

    /** Candidates for a brand-new Doctor profile: users with no role assigned yet. */
    public List<UserOptionDto> findEligibleDoctorUsers() {
        return toOptionDtos(userRepository.findUsersWithNoRoles());
    }

    /** Candidates for a brand-new Patient profile: users with no role assigned yet. */
    public List<UserOptionDto> findEligiblePatientUsers() {
        return toOptionDtos(userRepository.findUsersWithNoRoles());
    }

    /**
     * Candidates for "manager of a medical unit": users with no role
     * assigned yet. currentManagerId (nullable - pass the unit's existing
     * managerId when editing, or null when creating) is always included in
     * the result even if it doesn't strictly qualify any more, so saving
     * the form without touching the dropdown can never silently unassign
     * the current manager.
     */
    public List<UserOptionDto> findManagerCandidates(Long currentManagerId) {
        List<User> candidates = new ArrayList<>(userRepository.findUsersWithNoRoles());
        boolean alreadyIncluded = currentManagerId != null
                && candidates.stream().anyMatch(u -> u.getId().equals(currentManagerId));
        if (currentManagerId != null && !alreadyIncluded) {
            userRepository.findById(currentManagerId).ifPresent(candidates::add);
        }
        return toOptionDtos(candidates);
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

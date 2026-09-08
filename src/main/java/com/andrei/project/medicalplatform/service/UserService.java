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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getById(Long id) {
        return toResponseDto(getUserOrThrow(id));
    }

    public List<UserOptionDto> findEligibleDoctorUsers() {
        return toOptionDtos(userRepository.findUsersWithNoRoles());
    }

    public List<UserOptionDto> findEligiblePatientUsers() {
        return toOptionDtos(userRepository.findUsersWithNoRoles());
    }

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

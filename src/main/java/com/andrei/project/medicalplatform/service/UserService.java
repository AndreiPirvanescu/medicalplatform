package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.CreateUserRequestDTO;
import com.andrei.project.medicalplatform.dto.UserResponseDTO;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO dto) {
        userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();

        User saved = userRepository.save(user);

        return UserResponseDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .build();
    }


}

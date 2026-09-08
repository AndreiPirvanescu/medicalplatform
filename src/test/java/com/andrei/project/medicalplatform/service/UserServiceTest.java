package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.user.UserResponseDto;
import com.andrei.project.medicalplatform.model.Role;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * NOTE ON ASSUMPTIONS:
 * UserResponseDto / UserOptionDto are assumed to be Java records whose canonical
 * constructor takes the arguments in the exact order UserService passes them
 * (visible in the production source). Because records get generated equals()/hashCode(),
 * the tests build an "expected" instance the same way and compare with isEqualTo(),
 * so the exact accessor method names don't need to be guessed.
 * User/Role are assumed to be standard JPA entities with a no-arg constructor and
 * getters/setters that mirror each other (only getters are actually invoked by
 * UserService, so setters below are only used to build test fixtures).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildUser(Long id, String firstName, String lastName, String email, String phone, Set<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRoles(roles);
        return user;
    }

    @Test
    void getById_returnsMappedDto_whenUserExists() {
        Role role = mock(Role.class);
        when(role.getName()).thenReturn("ADMIN");
        User user = buildUser(1L, "Jane", "Doe", "jane@doe.com", "0700000000", Set.of(role));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getById(1L);

        UserResponseDto expected = new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPhone(), List.of("ADMIN"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getById_usesEmptyRoleList_whenRolesIsNull() {
        User user = buildUser(2L, "John", "Smith", "john@smith.com", "0711111111", null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getById(2L);

        assertThat(result).isEqualTo(new UserResponseDto(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPhone(), List.of()));
    }

    @Test
    void getById_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findEligibleDoctorUsers_mapsUsersWithNoRoles() {
        User user = buildUser(1L, "Jane", "Doe", "jane@doe.com", "0700000000", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(user));

        List<UserOptionDto> result = userService.findEligibleDoctorUsers();

        assertThat(result).containsExactly(
                new UserOptionDto(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail()));
    }

    @Test
    void findEligiblePatientUsers_mapsUsersWithNoRoles() {
        User user = buildUser(1L, "Jane", "Doe", "jane@doe.com", "0700000000", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(user));

        List<UserOptionDto> result = userService.findEligiblePatientUsers();

        assertThat(result).containsExactly(
                new UserOptionDto(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail()));
    }

    @Test
    void findManagerCandidates_returnsOnlyNoRoleUsers_whenManagerIdIsNull() {
        User candidate = buildUser(1L, "A", "B", "a@b.com", "01", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(candidate));

        List<UserOptionDto> result = userService.findManagerCandidates(null);

        assertThat(result).containsExactly(
                new UserOptionDto(candidate.getId(), "A B", "a@b.com"));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void findManagerCandidates_doesNotRefetch_whenCurrentManagerAlreadyInCandidateList() {
        User candidate = buildUser(5L, "A", "B", "a@b.com", "01", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(candidate));

        List<UserOptionDto> result = userService.findManagerCandidates(5L);

        assertThat(result).hasSize(1);
        verify(userRepository, never()).findById(eq(5L));
    }

    @Test
    void findManagerCandidates_appendsCurrentManager_whenNotAlreadyInCandidateList() {
        User candidate = buildUser(1L, "A", "B", "a@b.com", "01", Set.of());
        User currentManager = buildUser(7L, "C", "D", "c@d.com", "02", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(candidate));
        when(userRepository.findById(7L)).thenReturn(Optional.of(currentManager));

        List<UserOptionDto> result = userService.findManagerCandidates(7L);

        assertThat(result).containsExactly(
                new UserOptionDto(1L, "A B", "a@b.com"),
                new UserOptionDto(7L, "C D", "c@d.com"));
    }

    @Test
    void findManagerCandidates_doesNotAppend_whenCurrentManagerNotFound() {
        User candidate = buildUser(1L, "A", "B", "a@b.com", "01", Set.of());
        when(userRepository.findUsersWithNoRoles()).thenReturn(List.of(candidate));
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        List<UserOptionDto> result = userService.findManagerCandidates(7L);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(new UserOptionDto(1L, "A B", "a@b.com"));
    }
}

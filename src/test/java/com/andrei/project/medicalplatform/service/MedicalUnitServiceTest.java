package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitRequestDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.exception.EmailAlreadyExistsException;
import com.andrei.project.medicalplatform.exception.ManagerAlreadyAssignedException;
import com.andrei.project.medicalplatform.mapper.MedicalUnitMapper;
import com.andrei.project.medicalplatform.model.MedicalUnit;
import com.andrei.project.medicalplatform.model.User;
import com.andrei.project.medicalplatform.repository.MedicalUnitRepository;
import com.andrei.project.medicalplatform.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: MedicalUnitRequestDto(String name, String email, String phone, String address,
 * Long managerId) is a record whose accessors match the ones used in MedicalUnitService.
 */
@ExtendWith(MockitoExtension.class)
class MedicalUnitServiceTest {

    @Mock
    private MedicalUnitRepository medicalUnitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MedicalUnitMapper mapper;

    @InjectMocks
    private MedicalUnitService medicalUnitService;

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private MedicalUnitRequestDto request(String email, Long managerId) {
        return new MedicalUnitRequestDto("Central Clinic", email, "0700000000", "123 Main St", managerId);
    }

    // ---------- create ----------

    @Test
    void create_throws_whenEmailAlreadyExists() {
        when(medicalUnitRepository.existsByEmail("clinic@x.com")).thenReturn(true);

        assertThatThrownBy(() -> medicalUnitService.create(request("clinic@x.com", 1L)))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_throws_whenManagerNotFound() {
        when(medicalUnitRepository.existsByEmail("clinic@x.com")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalUnitService.create(request("clinic@x.com", 1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_throws_whenManagerAlreadyManagesAUnit() {
        when(medicalUnitRepository.existsByEmail("clinic@x.com")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(medicalUnitRepository.existsByManager_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> medicalUnitService.create(request("clinic@x.com", 1L)))
                .isInstanceOf(ManagerAlreadyAssignedException.class);
        verify(medicalUnitRepository, never()).save(any());
    }

    @Test
    void create_savesUnit_whenValid() {
        when(medicalUnitRepository.existsByEmail("clinic@x.com")).thenReturn(false);
        User manager = userWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(medicalUnitRepository.existsByManager_Id(1L)).thenReturn(false);
        MedicalUnit saved = new MedicalUnit();
        when(medicalUnitRepository.save(any(MedicalUnit.class))).thenReturn(saved);
        MedicalUnitResponseDto responseDto = mock(MedicalUnitResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        MedicalUnitResponseDto result = medicalUnitService.create(request("clinic@x.com", 1L));

        assertThat(result).isSameAs(responseDto);
    }

    // ---------- update ----------

    @Test
    void update_throws_whenUnitNotFound() {
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalUnitService.update(1L, request("clinic@x.com", 2L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_skipsManagerExistenceCheck_whenManagerUnchanged() {
        MedicalUnit unit = new MedicalUnit();
        unit.setManager(userWithId(1L));
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(medicalUnitRepository.save(unit)).thenReturn(unit);
        MedicalUnitResponseDto responseDto = mock(MedicalUnitResponseDto.class);
        when(mapper.toDto(unit)).thenReturn(responseDto);

        MedicalUnitResponseDto result = medicalUnitService.update(1L, request("clinic@x.com", 1L));

        verify(medicalUnitRepository, never()).existsByManager_Id(anyLong());
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_throws_whenNewManagerAlreadyAssignedElsewhere() {
        MedicalUnit unit = new MedicalUnit();
        unit.setManager(userWithId(1L));
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(medicalUnitRepository.existsByManager_Id(2L)).thenReturn(true);

        assertThatThrownBy(() -> medicalUnitService.update(1L, request("clinic@x.com", 2L)))
                .isInstanceOf(ManagerAlreadyAssignedException.class);
        verify(medicalUnitRepository, never()).save(any());
    }

    @Test
    void update_setsNewManager_whenChangedAndAvailable() {
        MedicalUnit unit = new MedicalUnit();
        unit.setManager(userWithId(1L));
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(medicalUnitRepository.existsByManager_Id(2L)).thenReturn(false);
        User newManager = userWithId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newManager));
        when(medicalUnitRepository.save(unit)).thenReturn(unit);
        when(mapper.toDto(unit)).thenReturn(mock(MedicalUnitResponseDto.class));

        medicalUnitService.update(1L, request("clinic@x.com", 2L));

        assertThat(unit.getManager()).isEqualTo(newManager);
    }

    // ---------- delete ----------

    @Test
    void delete_throws_whenNotFound() {
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalUnitService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesUnit_whenFound() {
        MedicalUnit unit = new MedicalUnit();
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.of(unit));

        medicalUnitService.delete(1L);

        verify(medicalUnitRepository).delete(unit);
    }

    // ---------- getById ----------

    @Test
    void getById_throws_whenNotFound() {
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalUnitService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_returnsMappedDto_whenFound() {
        MedicalUnit unit = new MedicalUnit();
        when(medicalUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        MedicalUnitResponseDto dto = mock(MedicalUnitResponseDto.class);
        when(mapper.toDto(unit)).thenReturn(dto);

        assertThat(medicalUnitService.getById(1L)).isSameAs(dto);
    }

    // ---------- getByManagerId ----------

    @Test
    void getByManagerId_throws_whenNoUnitFound() {
        when(medicalUnitRepository.findByManager_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalUnitService.getByManagerId(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByManagerId_returnsMappedDto_whenFound() {
        MedicalUnit unit = new MedicalUnit();
        when(medicalUnitRepository.findByManager_Id(1L)).thenReturn(Optional.of(unit));
        MedicalUnitResponseDto dto = mock(MedicalUnitResponseDto.class);
        when(mapper.toDto(unit)).thenReturn(dto);

        assertThat(medicalUnitService.getByManagerId(1L)).isSameAs(dto);
    }

    // ---------- getAll ----------

    @Test
    @SuppressWarnings("unchecked")
    void getAll_mapsPageOfResults() {
        MedicalUnit unit = new MedicalUnit();
        Pageable pageable = PageRequest.of(0, 5);
        Page<MedicalUnit> page = new PageImpl<>(List.of(unit));
        when(medicalUnitRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        MedicalUnitResponseDto dto = mock(MedicalUnitResponseDto.class);
        when(mapper.toDto(unit)).thenReturn(dto);

        Page<MedicalUnitResponseDto> result = medicalUnitService.getAll("Central", "City", pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }
}

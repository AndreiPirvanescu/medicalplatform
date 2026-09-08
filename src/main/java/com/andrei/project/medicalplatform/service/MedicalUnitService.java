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
import com.andrei.project.medicalplatform.repository.spec.MedicalUnitSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalUnitService {

    private final MedicalUnitRepository medicalUnitRepository;
    private final UserRepository userRepository;
    private final MedicalUnitMapper mapper;

    public MedicalUnitResponseDto create(MedicalUnitRequestDto request) {
        if (medicalUnitRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "A medical unit with email " + request.email() + " already exists");
        }
        User manager = getManagerOrThrow(request.managerId());
        if (medicalUnitRepository.existsByManager_Id(manager.getId())) {
            throw new ManagerAlreadyAssignedException(
                    "User with id " + manager.getId() + " already manages a medical unit");
        }

        MedicalUnit unit = new MedicalUnit();
        applyRequestToEntity(unit, request, manager);

        return mapper.toDto(medicalUnitRepository.save(unit));
    }

    public MedicalUnitResponseDto update(Long id, MedicalUnitRequestDto request) {
        MedicalUnit unit = getUnitOrThrow(id);

        if (!unit.getManager().getId().equals(request.managerId())
                && medicalUnitRepository.existsByManager_Id(request.managerId())) {
            throw new ManagerAlreadyAssignedException(
                    "User with id " + request.managerId() + " already manages a medical unit");
        }

        User manager = getManagerOrThrow(request.managerId());
        applyRequestToEntity(unit, request, manager);

        return mapper.toDto(medicalUnitRepository.save(unit));
    }

    public void delete(Long id) {
        MedicalUnit unit = getUnitOrThrow(id);
        medicalUnitRepository.delete(unit);
    }

    @Transactional(readOnly = true)
    public MedicalUnitResponseDto getById(Long id) {
        return mapper.toDto(getUnitOrThrow(id));
    }

    @Transactional(readOnly = true)
    public MedicalUnitResponseDto getByManagerId(Long managerId) {
        MedicalUnit unit = medicalUnitRepository.findByManager_Id(managerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No medical unit found for manager id " + managerId));
        return mapper.toDto(unit);
    }

    @Transactional(readOnly = true)
    public Page<MedicalUnitResponseDto> getAll(String name, String location, Pageable pageable) {
        return medicalUnitRepository
                .findAll(MedicalUnitSpecifications.filterBy(name, location), pageable)
                .map(mapper::toDto);
    }

    private MedicalUnit getUnitOrThrow(Long id) {
        return medicalUnitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medical unit not found with id " + id));
    }

    private User getManagerOrThrow(Long managerId) {
        return userRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + managerId));
    }

    private void applyRequestToEntity(MedicalUnit unit, MedicalUnitRequestDto request, User manager) {
        unit.setName(request.name());
        unit.setEmail(request.email());
        unit.setPhone(request.phone());
        unit.setAddress(request.address());
        unit.setManager(manager);
    }
}
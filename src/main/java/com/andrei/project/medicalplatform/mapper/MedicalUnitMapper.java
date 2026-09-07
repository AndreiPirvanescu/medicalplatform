package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.model.MedicalUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalUnitMapper {

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerFullName", expression =
            "java(unit.getManager() != null ? unit.getManager().getFirstName() + \" \" + unit.getManager().getLastName() : null)")
    @Mapping(target = "doctorCount", expression =
            "java(unit.getDoctors() != null ? unit.getDoctors().size() : 0)")
    MedicalUnitResponseDto toDto(MedicalUnit unit);
}
package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "medicalUnitId", source = "medicalUnit.id")
    @Mapping(target = "medicalUnitName", source = "medicalUnit.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "doctorFullName", expression =
            "java(doctor.getUser() != null ? doctor.getUser().getFirstName() + \" \" + doctor.getUser().getLastName() : null)")
    DoctorResponseDto toDto(Doctor doctor);
}
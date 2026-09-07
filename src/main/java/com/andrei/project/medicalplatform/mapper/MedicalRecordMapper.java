package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordResponseDto;
import com.andrei.project.medicalplatform.model.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientFullName", expression =
            "java(medicalRecord.getPatient().getUser().getFirstName() + \" \" + medicalRecord.getPatient().getUser().getLastName())")
    MedicalRecordResponseDto toDto(MedicalRecord medicalRecord);
}
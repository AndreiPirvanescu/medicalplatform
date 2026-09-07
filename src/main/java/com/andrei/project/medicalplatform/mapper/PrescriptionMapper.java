package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.prescription.MedicationSummaryDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionResponseDto;
import com.andrei.project.medicalplatform.model.Medication;
import com.andrei.project.medicalplatform.model.Prescription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientFullName", expression =
            "java(prescription.getPatient().getUser().getFirstName() + \" \" + prescription.getPatient().getUser().getLastName())")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorFullName", expression =
            "java(prescription.getDoctor().getUser().getFirstName() + \" \" + prescription.getDoctor().getUser().getLastName())")
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "medications", source = "medications")
    PrescriptionResponseDto toDto(Prescription prescription);

    MedicationSummaryDto toMedicationDto(Medication medication);
}
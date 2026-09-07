package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientFullName", expression =
            "java(appointment.getPatient().getUser().getFirstName() + \" \" + appointment.getPatient().getUser().getLastName())")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorFullName", expression =
            "java(appointment.getDoctor().getUser().getFirstName() + \" \" + appointment.getDoctor().getUser().getLastName())")
    AppointmentResponseDto toDto(Appointment appointment);
}
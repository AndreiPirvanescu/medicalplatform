package com.andrei.project.medicalplatform.mapper;

import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.model.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorFullName", expression =
            "java(schedule.getDoctor().getUser().getFirstName() + \" \" + schedule.getDoctor().getUser().getLastName())")
    ScheduleResponseDto toDto(Schedule schedule);
}
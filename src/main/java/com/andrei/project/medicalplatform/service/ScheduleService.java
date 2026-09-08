package com.andrei.project.medicalplatform.service;

import com.andrei.project.medicalplatform.dto.schedule.ScheduleRequestDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleUpdateDto;
import com.andrei.project.medicalplatform.exception.InvalidScheduleTimeException;
import com.andrei.project.medicalplatform.exception.ScheduleOverlapException;
import com.andrei.project.medicalplatform.mapper.ScheduleMapper;
import com.andrei.project.medicalplatform.model.Doctor;
import com.andrei.project.medicalplatform.model.Schedule;
import com.andrei.project.medicalplatform.repository.DoctorRepository;
import com.andrei.project.medicalplatform.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private static final Long NEW_SCHEDULE_ID = -1L;

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleMapper mapper;

    public ScheduleResponseDto create(Long doctorId, ScheduleRequestDto request) {
        Doctor doctor = getDoctorOrThrow(doctorId);

        validateTimeRange(request.startTime(), request.endTime());
        ensureNoOverlap(doctorId, request.startTime(), request.endTime(), NEW_SCHEDULE_ID);

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setAvailable(true);

        return mapper.toDto(scheduleRepository.save(schedule));
    }

    public ScheduleResponseDto update(Long id, ScheduleUpdateDto request) {
        Schedule schedule = getScheduleOrThrow(id);

        validateTimeRange(request.startTime(), request.endTime());

        boolean timeChanged = !schedule.getStartTime().equals(request.startTime())
                || !schedule.getEndTime().equals(request.endTime());

        if (timeChanged) {
            ensureNoOverlap(schedule.getDoctor().getId(), request.startTime(), request.endTime(), id);
        }

        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setAvailable(request.available());

        return mapper.toDto(scheduleRepository.save(schedule));
    }

    public void delete(Long id) {
        scheduleRepository.delete(getScheduleOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getByDoctor(Long doctorId) {
        getDoctorOrThrow(doctorId);
        return scheduleRepository.findByDoctor_IdOrderByStartTimeAsc(doctorId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getAvailableByDoctor(Long doctorId, LocalDateTime from, LocalDateTime to) {
        getDoctorOrThrow(doctorId);

        if (to.isBefore(from)) {
            throw new InvalidScheduleTimeException("'to' date must not be before 'from' date");
        }

        return scheduleRepository
                .findByDoctor_IdAndAvailableTrueAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                        doctorId, from, to)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new InvalidScheduleTimeException("End time must be after start time");
        }
    }

    private void ensureNoOverlap(Long doctorId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (scheduleRepository.existsOverlappingSchedule(doctorId, start, end, excludeId)) {
            throw new ScheduleOverlapException(
                    "Schedule overlaps with an existing time slot for this doctor");
        }
    }

    private Schedule getScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id " + id));
    }

    private Doctor getDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id " + id));
    }
}
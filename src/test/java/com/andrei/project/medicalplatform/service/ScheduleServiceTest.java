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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ASSUMPTIONS: ScheduleRequestDto(LocalDateTime startTime, LocalDateTime endTime) and
 * ScheduleUpdateDto(LocalDateTime startTime, LocalDateTime endTime, boolean available) are
 * records whose accessors match the method calls used inside ScheduleService
 * (startTime(), endTime(), available()). ScheduleResponseDto is opaque - the mapper is
 * mocked and we only assert the same instance is returned/propagated.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private ScheduleMapper mapper;

    @InjectMocks
    private ScheduleService scheduleService;

    private static final Long DOCTOR_ID = 1L;
    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 0);

    private Doctor doctorWithId(Long id) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        return doctor;
    }

    // ---------- create ----------

    @Test
    void create_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());
        ScheduleRequestDto request = new ScheduleRequestDto(start, end);

        assertThatThrownBy(() -> scheduleService.create(DOCTOR_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(DOCTOR_ID.toString());
        verifyNoInteractions(scheduleRepository);
    }

    @Test
    void create_throws_whenEndNotAfterStart() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        ScheduleRequestDto request = new ScheduleRequestDto(end, start);

        assertThatThrownBy(() -> scheduleService.create(DOCTOR_ID, request))
                .isInstanceOf(InvalidScheduleTimeException.class);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void create_throws_whenOverlapExists() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        when(scheduleRepository.existsOverlappingSchedule(DOCTOR_ID, start, end, -1L)).thenReturn(true);
        ScheduleRequestDto request = new ScheduleRequestDto(start, end);

        assertThatThrownBy(() -> scheduleService.create(DOCTOR_ID, request))
                .isInstanceOf(ScheduleOverlapException.class);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void create_savesSchedule_whenValidAndNoOverlap() {
        Doctor doctor = doctorWithId(DOCTOR_ID);
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.existsOverlappingSchedule(DOCTOR_ID, start, end, -1L)).thenReturn(false);
        Schedule saved = new Schedule();
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(saved);
        ScheduleResponseDto responseDto = mock(ScheduleResponseDto.class);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        ScheduleResponseDto result = scheduleService.create(DOCTOR_ID, new ScheduleRequestDto(start, end));

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(captor.capture());
        Schedule persisted = captor.getValue();
        assertThat(persisted.getDoctor()).isEqualTo(doctor);
        assertThat(persisted.getStartTime()).isEqualTo(start);
        assertThat(persisted.getEndTime()).isEqualTo(end);
        assertThat(persisted.getAvailable()).isTrue();
        assertThat(result).isSameAs(responseDto);
    }

    // ---------- update ----------

    @Test
    void update_throws_whenScheduleNotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.update(1L, new ScheduleUpdateDto(start, end, true)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_throws_whenEndNotAfterStart() {
        Schedule schedule = existingSchedule(start, end);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.update(1L, new ScheduleUpdateDto(end, start, true)))
                .isInstanceOf(InvalidScheduleTimeException.class);
    }

    @Test
    void update_skipsOverlapCheck_whenTimeUnchanged() {
        Schedule schedule = existingSchedule(start, end);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        ScheduleResponseDto responseDto = mock(ScheduleResponseDto.class);
        when(mapper.toDto(schedule)).thenReturn(responseDto);

        ScheduleResponseDto result = scheduleService.update(1L, new ScheduleUpdateDto(start, end, false));

        verify(scheduleRepository, never()).existsOverlappingSchedule(any(), any(), any(), any());
        assertThat(schedule.getAvailable()).isFalse();
        assertThat(result).isSameAs(responseDto);
    }

    @Test
    void update_checksOverlapExcludingSelf_whenTimeChanged() {
        Schedule schedule = existingSchedule(start, end);
        schedule.setDoctor(doctorWithId(DOCTOR_ID));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        LocalDateTime newStart = start.plusHours(1);
        LocalDateTime newEnd = end.plusHours(1);
        when(scheduleRepository.existsOverlappingSchedule(DOCTOR_ID, newStart, newEnd, 1L)).thenReturn(true);

        assertThatThrownBy(() -> scheduleService.update(1L, new ScheduleUpdateDto(newStart, newEnd, true)))
                .isInstanceOf(ScheduleOverlapException.class);
    }

    @Test
    void update_savesNewTimesAndAvailability_whenNoOverlap() {
        Schedule schedule = existingSchedule(start, end);
        schedule.setDoctor(doctorWithId(DOCTOR_ID));
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        LocalDateTime newStart = start.plusHours(1);
        LocalDateTime newEnd = end.plusHours(1);
        when(scheduleRepository.existsOverlappingSchedule(DOCTOR_ID, newStart, newEnd, 1L)).thenReturn(false);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        ScheduleResponseDto responseDto = mock(ScheduleResponseDto.class);
        when(mapper.toDto(schedule)).thenReturn(responseDto);

        ScheduleResponseDto result = scheduleService.update(1L, new ScheduleUpdateDto(newStart, newEnd, true));

        assertThat(schedule.getStartTime()).isEqualTo(newStart);
        assertThat(schedule.getEndTime()).isEqualTo(newEnd);
        assertThat(schedule.getAvailable()).isTrue();
        assertThat(result).isSameAs(responseDto);
    }

    private Schedule existingSchedule(LocalDateTime s, LocalDateTime e) {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setStartTime(s);
        schedule.setEndTime(e);
        schedule.setAvailable(true);
        return schedule;
    }

    // ---------- delete ----------

    @Test
    void delete_throws_whenNotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scheduleService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_removesSchedule_whenFound() {
        Schedule schedule = existingSchedule(start, end);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        scheduleService.delete(1L);

        verify(scheduleRepository).delete(schedule);
    }

    // ---------- getByDoctor ----------

    @Test
    void getByDoctor_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scheduleService.getByDoctor(DOCTOR_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByDoctor_mapsAllSchedules() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        Schedule s1 = existingSchedule(start, end);
        when(scheduleRepository.findByDoctor_IdOrderByStartTimeAsc(DOCTOR_ID)).thenReturn(List.of(s1));
        ScheduleResponseDto dto = mock(ScheduleResponseDto.class);
        when(mapper.toDto(s1)).thenReturn(dto);

        List<ScheduleResponseDto> result = scheduleService.getByDoctor(DOCTOR_ID);

        assertThat(result).containsExactly(dto);
    }

    // ---------- getAvailableByDoctor ----------

    @Test
    void getAvailableByDoctor_throws_whenDoctorNotFound() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scheduleService.getAvailableByDoctor(DOCTOR_ID, start, end))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAvailableByDoctor_throws_whenToBeforeFrom() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));

        assertThatThrownBy(() -> scheduleService.getAvailableByDoctor(DOCTOR_ID, end, start))
                .isInstanceOf(InvalidScheduleTimeException.class);
    }

    @Test
    void getAvailableByDoctor_returnsMappedResults() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctorWithId(DOCTOR_ID)));
        Schedule s1 = existingSchedule(start, end);
        when(scheduleRepository
                .findByDoctor_IdAndAvailableTrueAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                        DOCTOR_ID, start, end))
                .thenReturn(List.of(s1));
        ScheduleResponseDto dto = mock(ScheduleResponseDto.class);
        when(mapper.toDto(s1)).thenReturn(dto);

        List<ScheduleResponseDto> result = scheduleService.getAvailableByDoctor(DOCTOR_ID, start, end);

        assertThat(result).containsExactly(dto);
    }
}

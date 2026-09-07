package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctor_IdOrderByStartTimeAsc(Long doctorId);

    List<Schedule> findByDoctor_IdAndAvailableTrueAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Long doctorId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Schedule s
            WHERE s.doctor.id = :doctorId
            AND s.id <> :excludeId
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    boolean existsOverlappingSchedule(@Param("doctorId") Long doctorId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("excludeId") Long excludeId);
}
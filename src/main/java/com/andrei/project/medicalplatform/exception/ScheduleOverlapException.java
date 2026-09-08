package com.andrei.project.medicalplatform.exception;

public class ScheduleOverlapException extends RuntimeException {
    public ScheduleOverlapException(String message) {
        super(message);
    }
}
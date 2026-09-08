package com.andrei.project.medicalplatform.exception;

public class TimeSlotUnavailableException extends RuntimeException {
    public TimeSlotUnavailableException(String message) {
        super(message);
    }
}
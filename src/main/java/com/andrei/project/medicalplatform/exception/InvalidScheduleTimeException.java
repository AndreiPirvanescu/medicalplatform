package com.andrei.project.medicalplatform.exception;

public class InvalidScheduleTimeException extends RuntimeException {
    public InvalidScheduleTimeException(String message) {
        super(message);
    }
}
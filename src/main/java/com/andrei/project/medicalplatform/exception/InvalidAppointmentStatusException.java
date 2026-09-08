package com.andrei.project.medicalplatform.exception;

public class InvalidAppointmentStatusException extends RuntimeException {
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}
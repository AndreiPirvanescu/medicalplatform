package com.andrei.project.medicalplatform.exception;

public class InvalidPrescriptionAppointmentException extends RuntimeException {
    public InvalidPrescriptionAppointmentException(String message) {
        super(message);
    }
}
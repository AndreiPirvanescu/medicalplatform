package com.andrei.project.medicalplatform.exception;

public class MedicationNotInPrescriptionException extends RuntimeException {
    public MedicationNotInPrescriptionException(String message) {
        super(message);
    }
}
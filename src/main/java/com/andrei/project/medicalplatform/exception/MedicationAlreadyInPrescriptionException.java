package com.andrei.project.medicalplatform.exception;

public class MedicationAlreadyInPrescriptionException extends RuntimeException {
    public MedicationAlreadyInPrescriptionException(String message) {
        super(message);
    }
}
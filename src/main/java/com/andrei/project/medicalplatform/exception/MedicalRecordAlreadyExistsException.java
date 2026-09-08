package com.andrei.project.medicalplatform.exception;

public class MedicalRecordAlreadyExistsException extends RuntimeException {
    public MedicalRecordAlreadyExistsException(String message) {
        super(message);
    }
}
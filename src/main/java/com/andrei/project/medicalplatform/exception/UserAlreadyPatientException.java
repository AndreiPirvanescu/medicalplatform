package com.andrei.project.medicalplatform.exception;

public class UserAlreadyPatientException extends RuntimeException {
    public UserAlreadyPatientException(String message) {
        super(message);
    }
}
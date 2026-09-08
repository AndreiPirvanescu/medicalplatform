package com.andrei.project.medicalplatform.exception;

public class UserAlreadyDoctorException extends RuntimeException {
    public UserAlreadyDoctorException(String message) {
        super(message);
    }
}
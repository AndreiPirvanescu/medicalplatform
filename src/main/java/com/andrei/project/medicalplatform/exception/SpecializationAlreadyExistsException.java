package com.andrei.project.medicalplatform.exception;

public class SpecializationAlreadyExistsException extends RuntimeException {
    public SpecializationAlreadyExistsException(String message) {
        super(message);
    }
}
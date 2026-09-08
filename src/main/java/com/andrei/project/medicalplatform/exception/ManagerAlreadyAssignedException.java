package com.andrei.project.medicalplatform.exception;

public class ManagerAlreadyAssignedException extends RuntimeException {
    public ManagerAlreadyAssignedException(String message) {
        super(message);
    }
}
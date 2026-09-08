package com.andrei.project.medicalplatform.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) { super(message); }
}
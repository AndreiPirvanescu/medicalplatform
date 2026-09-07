package com.andrei.project.medicalplatform.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidxception(MethodArgumentNotValidException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error.");
        var errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        problem.setDetail("Object did not pass validation checks.");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Entity not found.");
        problem.setDetail(e.getMessage());
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Email already exists.");
        problem.setDetail(e.getMessage());
        return problem;
    }

    @ExceptionHandler(ManagerAlreadyAssignedException.class)
    public ProblemDetail handleManagerAlreadyAssignedException(ManagerAlreadyAssignedException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Manager already assigned.");
        problem.setDetail(e.getMessage());
        return problem;
    }

    @ExceptionHandler(UserAlreadyDoctorException.class)
    public ProblemDetail handleUserAlreadyDoctorException(UserAlreadyDoctorException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("User already a doctor.");
        problem.setDetail(e.getMessage());
        return problem;
    }

    @ExceptionHandler(SpecializationAlreadyExistsException.class)
    public ProblemDetail handleSpecializationAlreadyExistsException(SpecializationAlreadyExistsException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Specialization already assigned.");
        problem.setDetail(e.getMessage());
        return problem;
    }

    @ExceptionHandler(UserAlreadyPatientException.class)
    public ProblemDetail handleUserAlreadyPatientException(UserAlreadyPatientException e) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("User already a patient.");
        problem.setDetail(e.getMessage());
        return problem;
    }


}

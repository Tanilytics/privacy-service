package com.tanalytics.privacy.controller;

import com.tanalytics.privacy.auth.InternalAuthUnavailableException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setTitle("Validation error");
        detail.setProperty("errorCode", "REQUEST_VALIDATION_ERROR");
        return detail;
    }

    @ExceptionHandler(InternalAuthUnavailableException.class)
    public ProblemDetail handleInternalAuth(InternalAuthUnavailableException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        detail.setTitle("Authorization dependency unavailable");
        detail.setProperty("errorCode", "AUTH_DEPENDENCY_UNAVAILABLE");
        return detail;
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Datastore unavailable");
        detail.setTitle("Datastore unavailable");
        detail.setProperty("errorCode", "DATASTORE_UNAVAILABLE");
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid request");
        detail.setProperty("errorCode", "INVALID_REQUEST");
        return detail;
    }
}

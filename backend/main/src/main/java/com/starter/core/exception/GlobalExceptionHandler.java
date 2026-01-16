package com.starter.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.starter.core.common.dto.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

/** Global exception handler for REST controllers. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed: {}", errors);

        return ErrorResponse.builder()
            .error("VALIDATION_ERROR")
            .message("Validation failed")
            .details(errors)
            .build();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email already exists: {}", ex.getEmail());

        return ErrorResponse.builder()
            .error("EMAIL_ALREADY_EXISTS")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return ErrorResponse.builder()
            .error("INVALID_CREDENTIALS")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("INVALID_TOKEN")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(com.starter.core.email.EmailService.EmailSendException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleEmailSendException(
        com.starter.core.email.EmailService.EmailSendException ex
    ) {
        log.error("Email send failed: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("EMAIL_SEND_FAILED")
            .message("Failed to send email. Please try again later.")
            .build();
    }

    @ExceptionHandler(AdminOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAdminOperationException(AdminOperationException ex) {
        log.warn("Admin operation denied: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("ADMIN_OPERATION_DENIED")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("INVALID_ARGUMENT")
            .message(ex.getMessage())
            .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        return ErrorResponse.builder()
            .error("ACCESS_DENIED")
            .message("You do not have permission to access this resource")
            .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        return ErrorResponse.builder()
            .error("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .build();
    }
}

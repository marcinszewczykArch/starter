package com.starter.core.exception;

/** Exception thrown when an admin operation is not allowed. */
public class AdminOperationException extends RuntimeException {

    public AdminOperationException(String message) {
        super(message);
    }
}

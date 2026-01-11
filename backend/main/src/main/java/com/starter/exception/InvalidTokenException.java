package com.starter.exception;

/** Exception thrown when a verification or reset token is invalid or expired. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}

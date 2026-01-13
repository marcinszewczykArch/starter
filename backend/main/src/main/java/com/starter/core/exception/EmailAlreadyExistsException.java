package com.starter.core.exception;

/** Exception thrown when attempting to register with an email that already exists. */
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

package com.example.task.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

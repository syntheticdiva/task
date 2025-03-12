package com.example.task.exception;

public class ConfigFileNotFoundException extends RuntimeException {
    public ConfigFileNotFoundException(String message) {
        super(message);
    }
}
package com.example.summarizer.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) { super(message); }
    public InvalidInputException(String message, Throwable cause) { super(message, cause); }
}


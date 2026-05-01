package com.orchestrix.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}

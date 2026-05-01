package com.orchestrix.exception;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}

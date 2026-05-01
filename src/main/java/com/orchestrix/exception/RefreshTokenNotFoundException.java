package com.orchestrix.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}

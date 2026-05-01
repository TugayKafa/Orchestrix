package com.orchestrix.exception;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}

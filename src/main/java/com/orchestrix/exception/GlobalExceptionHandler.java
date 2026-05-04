package com.orchestrix.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException exc) {
        logger.warn("Registration failed: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidPassword(InvalidPasswordException exc) {
        logger.warn("Login failed: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exc) {
        logger.warn("Login failed: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException exc) {
        String message = exc.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException exc) {
        logger.warn("Expired JWT token used");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtException(JwtException exc) {
        logger.warn("Invalid JWT token: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<String> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException exc) {
        logger.warn("Refresh token not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<String> handleRefreshTokenRevokedException(RefreshTokenRevokedException exc) {
        logger.warn("Revoked refresh token used");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<String> handleRefreshTokenExpiredException(RefreshTokenExpiredException exc) {
        logger.warn("Expired refresh token used");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }
}

package com.payaza.nps.exception;

import com.payaza.nps.dto.ErrorResponseDto;
import com.payaza.nps.security.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for API errors
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String clientId = ClientContext.getCurrentClientId();
        logger.warn("Access denied for client '{}' to '{}': {}", clientId, request.getRequestURI(), ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
            "ACCESS_DENIED",
            "Insufficient permissions to access this resource",
            request.getRequestURI(),
            clientId
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        logger.warn("Bad credentials for request to '{}': {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
            "INVALID_API_KEY",
            "Invalid or missing API key",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String clientId = ClientContext.getCurrentClientId();
        logger.warn("Illegal argument for client '{}' to '{}': {}", clientId, request.getRequestURI(), ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
            "INVALID_REQUEST",
            ex.getMessage(),
            request.getRequestURI(),
            clientId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        String clientId = ClientContext.getCurrentClientId();
        logger.warn("Illegal state for client '{}' to '{}': {}", clientId, request.getRequestURI(), ex.getMessage());
        
        ErrorResponseDto error = new ErrorResponseDto(
            "INVALID_STATE",
            ex.getMessage(),
            request.getRequestURI(),
            clientId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String clientId = ClientContext.getCurrentClientId();
        logger.warn("Validation errors for client '{}' to '{}': {}", clientId, request.getRequestURI(), ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponseDto error = new ErrorResponseDto(
            "VALIDATION_ERROR",
            "Request validation failed: " + errors.toString(),
            request.getRequestURI(),
            clientId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        String clientId = ClientContext.getCurrentClientId();
        logger.error("Unexpected error for client '{}' to '{}': {}", clientId, request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponseDto error = new ErrorResponseDto(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI(),
            clientId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
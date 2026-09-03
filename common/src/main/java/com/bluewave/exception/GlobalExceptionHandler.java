package com.bluewave.exception;

import com.bluewave.dto.CommonApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private <T> ResponseEntity<CommonApiResponse<T>> buildErrorResponse(String message, HttpStatus status, T data) {
        CommonApiResponse<T> response = CommonApiResponse.<T>builder()
                .status(String.valueOf(status.value()))
                .success(false)
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleConflict(ResourceConflictException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler(UnauthorizationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAuthorization(UnauthorizationException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, null);
    }

    @ExceptionHandler(GernalServerError.class)
    public ResponseEntity<CommonApiResponse<Void>> handleGeneralServerError(GernalServerError ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildErrorResponse("Validation failed for one or more fields", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleGenericException(Exception ex) {
        return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
}
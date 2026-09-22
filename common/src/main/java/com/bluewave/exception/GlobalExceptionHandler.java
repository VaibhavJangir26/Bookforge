package com.bluewave.exception;

import com.bluewave.dto.CommonApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private <T> ResponseEntity<CommonApiResponse<T>> buildErrorResponse(String message, HttpStatus status, T data) {
        CommonApiResponse<T> response = CommonApiResponse.<T>builder()
                .status(status.value())
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String msg = "Malformed JSON request or invalid field format: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
        return buildErrorResponse(msg, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleMissingParams(MissingServletRequestParameterException ex) {
        return buildErrorResponse("Missing required request parameter: " + ex.getParameterName(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleMissingParts(MissingServletRequestPartException ex) {
        return buildErrorResponse("Missing required request part: " + ex.getRequestPartName(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, null);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleTooManyRequest(TooManyRequestException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS, null);
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse("You do not have permission to access this resource", HttpStatus.FORBIDDEN, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleGenericException(Exception ex) {
        return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
}
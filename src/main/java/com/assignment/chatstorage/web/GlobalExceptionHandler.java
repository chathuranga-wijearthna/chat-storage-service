package com.assignment.chatstorage.web;

import com.assignment.chatstorage.exception.CustomGlobalException;
import com.assignment.chatstorage.exception.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            errors.put(field, err.getDefaultMessage());
        });
        body.put("detail", "Validation failed");
        body.put("errors", errors);
        log.warn("Validation failed: {} field error(s)", errors.size());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CustomGlobalException.class)
    public ResponseEntity<?> handleCustomGlobalException(CustomGlobalException ex) {
        HttpStatus status = mapStatus(ex.getCode());
        log.warn("Domain error code={} status={} message={}", ex.getCode(), status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("detail", "Internal error"));
    }

    private HttpStatus mapStatus(String code) {
        if (ErrorCode.SESSION_NOT_FOUND.getCode().equals(code)) {
            return HttpStatus.NOT_FOUND;
        }

        // Default to BAD_REQUEST for input-related errors
        return HttpStatus.BAD_REQUEST;
    }
}

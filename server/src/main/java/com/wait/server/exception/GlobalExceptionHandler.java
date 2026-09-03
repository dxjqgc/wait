package com.wait.server.exception;

import com.wait.server.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusiness(BusinessException ex) {
        log.warn("business exception: code={}, msg={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.ok(R.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(R.fail(400, msg));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(R.fail(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleOther(Exception ex) {
        log.error("unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(500, "server error"));
    }
}

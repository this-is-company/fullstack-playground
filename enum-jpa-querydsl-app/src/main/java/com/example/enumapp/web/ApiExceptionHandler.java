package com.example.enumapp.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** 비즈니스 오류 → 4xx */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleBusiness(BusinessException ex) {
        int status = ex.getStatus().value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ex.getMessageCode(), ex.getMessage())
        );
    }

    /** 입력 검증 실패 → 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage())
        );

        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors)
        );
    }

    /** 기타 잘못된 인자 → 400 (BusinessException 으로 안 감싼 경우) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleIllegalArgument(IllegalArgumentException ex) {
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.BAD_REQUEST, ex.getMessage())
        );
    }

    /** 서버 오류 → 5xx (상세 메시지는 로그에만) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleUnexpected(Exception ex) {
        log.error("Unhandled server error", ex);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.INTERNAL_ERROR, "Internal server error")
        );
    }
}

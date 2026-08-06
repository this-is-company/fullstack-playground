package com.example.enumapp.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    /** 입력 검증 실패 (POST body) → 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleValidation(MethodArgumentNotValidException ex) {
        return validationFailed(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
    }

    /** GET @ModelAttribute 바인딩/검증 실패 → 400 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleBind(BindException ex) {
        return validationFailed(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
    }

    /** GET @RequestParam / @Validated 메서드 파라미터 ConstraintViolation → 400 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath() == null ? "request" : violation.getPropertyPath().toString();
            // "search.arg0.ids" / "requiredIds.ids" → 마지막 세그먼트 위주
            int dot = path.lastIndexOf('.');
            String key = dot >= 0 ? path.substring(dot + 1) : path;
            fieldErrors.put(key, violation.getMessage());
        }
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors)
        );
    }

    /** 필수 쿼리 파라미터 누락 → 400 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(ex.getParameterName(), "required parameter is missing");
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors)
        );
    }

    /** 날짜/숫자 등 타입 변환 실패 (예: orderDate=not-a-date) → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ApiErrorBody>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        String name = ex.getName() == null ? "param" : ex.getName();
        String required = ex.getRequiredType() == null ? "unknown" : ex.getRequiredType().getSimpleName();
        fieldErrors.put(name, "invalid value for type " + required + ": " + ex.getValue());
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors)
        );
    }

    private ResponseEntity<ApiResponse<ApiErrorBody>> validationFailed(
            Iterable<FieldError> fieldErrors,
            Iterable<? extends org.springframework.validation.ObjectError> globalErrors
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        globalErrors.forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(
                ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", errors)
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

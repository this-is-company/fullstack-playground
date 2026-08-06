package com.example.enumapp.web

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    /** 비즈니스 오류 → 4xx */
    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val status = ex.status.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ex.messageCode, ex.message),
        )
    }

    /** 입력 검증 실패 (POST body) → 400 */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<ApiErrorBody>> =
        validationFailed(ex.bindingResult.fieldErrors, ex.bindingResult.globalErrors)

    /** GET @ModelAttribute 바인딩/검증 실패 → 400 */
    @ExceptionHandler(BindException::class)
    fun handleBind(ex: BindException): ResponseEntity<ApiResponse<ApiErrorBody>> =
        validationFailed(ex.bindingResult.fieldErrors, ex.bindingResult.globalErrors)

    /** GET @RequestParam / @Validated 메서드 파라미터 ConstraintViolation → 400 */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val fieldErrors = LinkedHashMap<String, String>()
        for (violation in ex.constraintViolations) {
            val path = violation.propertyPath?.toString() ?: "request"
            // "search.arg0.ids" / "requiredIds.ids" → 마지막 세그먼트 위주
            val dot = path.lastIndexOf('.')
            val key = if (dot >= 0) path.substring(dot + 1) else path
            fieldErrors[key] = violation.message
        }
        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors),
        )
    }

    /** 필수 쿼리 파라미터 누락 → 400 */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val fieldErrors = LinkedHashMap<String, String>()
        fieldErrors[ex.parameterName] = "required parameter is missing"
        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors),
        )
    }

    /** 날짜/숫자 등 타입 변환 실패 (예: orderDate=not-a-date) → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val fieldErrors = LinkedHashMap<String, String>()
        val name = ex.name
        val required = ex.requiredType?.simpleName ?: "unknown"
        fieldErrors[name] = "invalid value for type $required: ${ex.value}"
        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors),
        )
    }

    private fun validationFailed(
        fieldErrors: Iterable<FieldError>,
        globalErrors: Iterable<ObjectError>,
    ): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val errors = LinkedHashMap<String, String>()
        for (error in fieldErrors) {
            errors[error.field] = error.defaultMessage ?: ""
        }
        globalErrors.forEach { error ->
            errors[error.objectName] = error.defaultMessage ?: ""
        }
        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", errors),
        )
    }

    /** 기타 잘못된 인자 → 400 (BusinessException 으로 안 감싼 경우) */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.BAD_REQUEST, ex.message),
        )
    }

    /** 서버 오류 → 5xx (상세 메시지는 로그에만) */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiResponse<ApiErrorBody>> {
        log.error("Unhandled server error", ex)
        val status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.INTERNAL_ERROR, "Internal server error"),
        )
    }
}

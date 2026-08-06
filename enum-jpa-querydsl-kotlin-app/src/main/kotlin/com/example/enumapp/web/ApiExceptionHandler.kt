package com.example.enumapp.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

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

    /** 입력 검증 실패 → 400 */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<ApiErrorBody>> {
        val fieldErrors = LinkedHashMap<String, String>()
        for (error in ex.bindingResult.fieldErrors) {
            fieldErrors[error.field] = error.defaultMessage ?: ""
        }
        ex.bindingResult.globalErrors.forEach { error ->
            fieldErrors[error.objectName] = error.defaultMessage ?: ""
        }

        val status = HttpStatus.BAD_REQUEST.value()
        return ResponseEntity.status(status).body(
            ApiResponse.fail(status, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", fieldErrors),
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

package com.example.enumapp.web

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 성공/실패 공통 API 응답 구조.
 * <pre>
 * 성공:
 * {
 *   "status": 200,
 *   "error": false,
 *   "result": { ... }
 * }
 *
 * 실패:
 * {
 *   "status": 400,
 *   "error": true,
 *   "result": {
 *     "message": "...",
 *     "messageCode": "VALIDATION_ERROR",
 *     "errors": { "field": "..." }
 *   }
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
class ApiResponse<T> private constructor(
    val status: Int,
    val error: Boolean,
    val result: T?,
) {
    companion object {
        fun <T> ok(status: Int, result: T?): ApiResponse<T> =
            ApiResponse(status, false, result)

        fun fail(status: Int, messageCode: String?, message: String?): ApiResponse<ApiErrorBody> =
            fail(status, messageCode, message, null)

        fun fail(
            status: Int,
            messageCode: String?,
            message: String?,
            errors: Map<String, String>?,
        ): ApiResponse<ApiErrorBody> =
            ApiResponse(status, true, ApiErrorBody.of(messageCode, message, errors))
    }
}

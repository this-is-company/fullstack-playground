package com.example.enumapp.web;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class ApiResponse<T> {

    private int status;
    private boolean error;
    private T result;

    public static <T> ApiResponse<T> ok(int status, T result) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = status;
        response.error = false;
        response.result = result;
        return response;
    }

    public static ApiResponse<ApiErrorBody> fail(int status, String messageCode, String message) {
        return fail(status, messageCode, message, null);
    }

    public static ApiResponse<ApiErrorBody> fail(
            int status,
            String messageCode,
            String message,
            java.util.Map<String, String> errors
    ) {
        ApiResponse<ApiErrorBody> response = new ApiResponse<>();
        response.status = status;
        response.error = true;
        response.result = ApiErrorBody.of(messageCode, message, errors);
        return response;
    }

    public int getStatus() {
        return status;
    }

    public boolean isError() {
        return error;
    }

    public T getResult() {
        return result;
    }
}

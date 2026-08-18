package com.example.demo.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 성공/실패 공통 봉투. HTTP 상태코드와 별개로 body 형태는 항상 같다.
 * <pre>
 * 성공: { "status": "SUCCESS", "data": { ... } | [ ... ], "error": null }
 * 실패: { "status": "BUSINESS_ERROR"|"SERVER_ERROR", "data": null, "error": { "code", "message" } }
 * </pre>
 */
@Schema(description = "공통 API 응답")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    @Schema(description = "SUCCESS / BUSINESS_ERROR / SERVER_ERROR")
    private ApiStatus status;

    @Schema(description = "성공 시 본문. 객체 또는 리스트. 오류면 null")
    private T data;

    @Schema(description = "오류 시 code, message. 성공이면 null")
    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> body = new ApiResponse<>();
        body.status = ApiStatus.SUCCESS;
        body.data = data;
        body.error = null;
        return body;
    }

    public static <T> ApiResponse<T> businessError(String code, String message) {
        return fail(ApiStatus.BUSINESS_ERROR, code, message);
    }

    public static <T> ApiResponse<T> serverError(String code, String message) {
        return fail(ApiStatus.SERVER_ERROR, code, message);
    }

    private static <T> ApiResponse<T> fail(ApiStatus status, String code, String message) {
        ApiResponse<T> body = new ApiResponse<>();
        body.status = status;
        body.data = null;
        body.error = new ApiError(code, message);
        return body;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }
}

package com.example.enumapp.web

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 오류 시 [ApiResponse.result] 에 담기는 본문.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiErrorBody private constructor(
    val message: String?,
    val messageCode: String?,
    val errors: Map<String, String>?,
) {
    companion object {
        fun of(messageCode: String?, message: String?, errors: Map<String, String>?): ApiErrorBody =
            ApiErrorBody(message, messageCode, errors)
    }
}

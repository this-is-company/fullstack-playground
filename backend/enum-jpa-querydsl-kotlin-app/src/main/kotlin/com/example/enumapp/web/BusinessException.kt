package com.example.enumapp.web

import org.springframework.http.HttpStatus

/**
 * 클라이언트/도메인 규칙 위반(4xx). 서버 장애(5xx)와 구분한다.
 */
class BusinessException(
    val status: HttpStatus,
    val messageCode: String,
    message: String,
) : RuntimeException(message) {

    init {
        require(status.is4xxClientError) { "BusinessException status must be 4xx" }
    }

    companion object {
        fun badRequest(messageCode: String, message: String): BusinessException =
            BusinessException(HttpStatus.BAD_REQUEST, messageCode, message)

        fun notFound(messageCode: String, message: String): BusinessException =
            BusinessException(HttpStatus.NOT_FOUND, messageCode, message)
    }
}

package com.example.enumapp.web

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * Controller 정상 응답을 [ApiResponse] 로 감싼다.
 * ExceptionHandler 가 이미 ApiResponse 를 반환한 경우는 재포장하지 않는다.
 */
@RestControllerAdvice(basePackages = ["com.example.enumapp.web"])
class ApiResponseAdvice : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any {
        if (body is ApiResponse<*>) {
            return body
        }
        return ApiResponse.ok(resolveStatus(response), body)
    }

    private fun resolveStatus(response: ServerHttpResponse): Int {
        if (response is ServletServerHttpResponse) {
            val status = response.servletResponse.status
            if (status > 0) {
                return status
            }
        }
        return HttpStatus.OK.value()
    }
}

package com.example.enumapp.web;

import org.springframework.http.HttpStatus;

/**
 * 클라이언트/도메인 규칙 위반(4xx). 서버 장애(5xx)와 구분한다.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String messageCode;

    public BusinessException(HttpStatus status, String messageCode, String message) {
        super(message);
        if (status == null || !status.is4xxClientError()) {
            throw new IllegalArgumentException("BusinessException status must be 4xx");
        }
        this.status = status;
        this.messageCode = messageCode;
    }

    public static BusinessException badRequest(String messageCode, String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, messageCode, message);
    }

    public static BusinessException notFound(String messageCode, String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, messageCode, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessageCode() {
        return messageCode;
    }
}

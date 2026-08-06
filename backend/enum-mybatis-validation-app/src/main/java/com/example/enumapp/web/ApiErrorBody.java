package com.example.enumapp.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 오류 시 {@link ApiResponse#getResult()} 에 담기는 본문.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorBody {

    private String message;
    private String messageCode;
    private Map<String, String> errors;

    public static ApiErrorBody of(String messageCode, String message, Map<String, String> errors) {
        ApiErrorBody body = new ApiErrorBody();
        body.messageCode = messageCode;
        body.message = message;
        body.errors = errors;
        return body;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

package com.example.enumapp.web;

public final class ApiMessageCodes {

    /** 요청 검증 실패 (400) */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    /** 잘못된 파라미터/검색 조건 (400) */
    public static final String BAD_REQUEST = "BAD_REQUEST";
    /** 리소스 없음 (404) */
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
    /** 서버 내부 오류 (500) */
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ApiMessageCodes() {
    }
}

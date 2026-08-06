package com.example.enumapp.web

object ApiMessageCodes {
    /** 요청 검증 실패 (400) */
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    /** 잘못된 파라미터/검색 조건 (400) */
    const val BAD_REQUEST = "BAD_REQUEST"
    /** 리소스 없음 (404) */
    const val ORDER_NOT_FOUND = "ORDER_NOT_FOUND"
    const val FILE_NOT_FOUND = "FILE_NOT_FOUND"
    /** 서버 내부 오류 (500) */
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
}

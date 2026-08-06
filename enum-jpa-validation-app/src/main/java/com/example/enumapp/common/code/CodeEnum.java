package com.example.enumapp.common.code;

/**
 * DB/API 코드값을 가지는 Enum 추상화.
 * MyBatis TypeHandler 와 Jackson Converter 의 공통 대상이다.
 */
public interface CodeEnum {
    String getCode();
    String getDescription();
}

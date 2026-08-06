package com.example.apicommon;

/**
 * 여러 앱이 공유하는 API 경로 상수.
 * {@code @RequestMapping}에는 컴파일 타임 상수만 올 수 있다.
 */
public final class ApiPaths {

    public static final String API = "/api";
    public static final String USERS = "/users";
    public static final String USERS_FULL = API + USERS;
    public static final String HEALTH = "/health";

    private ApiPaths() {
    }
}

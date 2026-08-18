# api-mybatis-app

MyBatis 조회 + Swagger + 공통 응답 `{ status, data, error }`

| | |
|--|--|
| 포트 | **8130** |
| Swagger UI | **http://localhost:8130/swagger-ui.html** |
| OpenAPI JSON | http://localhost:8130/v3/api-docs |

Swagger 요청/응답은 **Schema** 탭이 기본. 맨 아래 **Schemas** 는 표(필드 / 타입·설명)로 보이도록 CSS 를 넣었다.

```bash
./gradlew bootRun
# 개발(SQL 로그 끔)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

컨트롤러는 `ApiResponse` 를 쓰지 않고 서비스 결과만 반환한다.
`ApiResponseAdvice` 가 성공 body 를 `{ status, data, error }` 로 감싼다. 예외는 `ApiExceptionHandler`.

## 응답

성공/실패 **body 형태는 동일**. `status` 로 구분.

```json
{ "status": "SUCCESS", "data": { }, "error": null }
{ "status": "SUCCESS", "data": [ ], "error": null }
{ "status": "BUSINESS_ERROR", "data": null, "error": { "code": "PRODUCT_NOT_FOUND", "message": "..." } }
{ "status": "SERVER_ERROR", "data": null, "error": { "code": "INTERNAL_ERROR", "message": "internal server error" } }
```

- `SUCCESS` — 정상 (`data` 는 객체 또는 리스트)
- `BUSINESS_ERROR` — 4xx (404 포함)
- `SERVER_ERROR` — 5xx

## SQL 로그 (local만)

`?` 에 값이 들어간 쿼리가 나와서 그대로 복사해 실행할 수 있다.

```
[MYBATIS→DB] ...ProductMapper.findById
SELECT id, name, price, stock
FROM demo_products
WHERE id = 1
```

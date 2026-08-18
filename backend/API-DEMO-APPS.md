# API 데모 3종 (MyBatis / JPA / QueryDSL)

새로 추가한 프로젝트. 조회 스택만 다르고 API·응답·Swagger 는 같다.

| 앱 | 포트 | Swagger |
|----|------|---------|
| [api-mybatis-app](api-mybatis-app/) | 8130 | http://localhost:8130/swagger-ui.html |
| [api-jpa-app](api-jpa-app/) | 8131 | http://localhost:8131/swagger-ui.html |
| [api-querydsl-app](api-querydsl-app/) | 8132 | http://localhost:8132/swagger-ui.html |

```bash
cd api-mybatis-app && ./gradlew bootRun
cd api-jpa-app && ./gradlew bootRun
cd api-querydsl-app && ./gradlew bootRun
# SQL 로그 끄기
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 공통 응답

```json
{ "status": "SUCCESS", "data": {}, "error": null }
{ "status": "SUCCESS", "data": [], "error": null }
{ "status": "BUSINESS_ERROR", "data": null, "error": { "code": "PRODUCT_NOT_FOUND", "message": "..." } }
{ "status": "SERVER_ERROR", "data": null, "error": { "code": "INTERNAL_ERROR", "message": "internal server error" } }
```

| status | 의미 | HTTP 예 |
|--------|------|---------|
| SUCCESS | 정상. `data` 는 객체 또는 리스트 | 200, 201 |
| BUSINESS_ERROR | 비즈니스/검증/404 | 400, 404 |
| SERVER_ERROR | 서버 오류 | 500 |

## SQL 로그

- **local** (기본): 프리티 SQL + Params
- **dev** 부터: 로그 없음

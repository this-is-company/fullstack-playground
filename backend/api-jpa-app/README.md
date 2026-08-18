# api-jpa-app

JPA 조회 + Swagger + 공통 응답 `{ status, data, error }`

| | |
|--|--|
| 포트 | **8131** |
| Swagger UI | **http://localhost:8131/swagger-ui.html** |
| OpenAPI JSON | http://localhost:8131/v3/api-docs |

Swagger 요청/응답은 **Schema** 탭이 기본. 맨 아래 **Schemas** 는 표 형태로 보이게 해 두었다.

```bash
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=dev'
```

컨트롤러는 서비스 값만 반환하고 `ApiResponseAdvice` 가 봉투를 만든다. 형식은 [api-mybatis-app](../api-mybatis-app/README.md) 과 같다.

local 에서 `[JPA→DB]` 에 값이 채워진 SQL 이 나와 그대로 복사 실행 가능. dev 부터는 없음.

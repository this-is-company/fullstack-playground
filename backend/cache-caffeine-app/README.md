# cache-caffeine-app

인메모리 **Caffeine** 만 사용. Redis 없음.

- 포트: **8110**
- DB: H2 (MyBatis)
- Swagger: http://localhost:8110/swagger-ui.html

```bash
./gradlew bootRun
```

1. `GET /api/products/1` → 로그 `[CACHE MISS]` + `[MYBATIS→DB] SELECT ...`
2. 같은 URL 다시 → **SQL 로그 없음** (Caffeine hit)
3. `DELETE /api/demo/cache/products/1` → 다음 GET 은 다시 SQL

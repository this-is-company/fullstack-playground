# cache-redis-app

**Redis만** 사용. 앱을 여러 대 띄워도 캐시가 공유된다.

- 포트: **8111**
- DB: H2 (MyBatis)
- Redis: `localhost:6379`

```bash
cd ../   # backend
docker compose up -d redis

cd cache-redis-app
./gradlew bootRun
```

1. `GET /api/products/1` → `[CACHE MISS]` + `[MYBATIS→DB]`
2. 다시 GET → **SQL 없음** (Redis hit)
3. `docker exec -it enum-demo-redis redis-cli KEYS *`
4. `DELETE /api/demo/cache/products/1` → 다음 GET 은 다시 SQL

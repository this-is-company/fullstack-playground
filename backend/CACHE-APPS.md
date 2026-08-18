# 캐시 전용 샘플 (Caffeine / Redis / L1+L2)

세 앱 모두 **MyBatis** 이고, SQL 이 나갈 때 반드시 로그가 찍힌다.

```
[MYBATIS→DB] ... SELECT ...   ← 이 줄이 있으면 DB를 탄 것
[CACHE MISS]                  ← 캐시 없음 → 곧 SQL
[CACHE HIT L1-Caffeine]       ← 로컬 메모리 (SQL 없음)
[CACHE HIT L2-Redis]          ← Redis (SQL 없음)
```

| 앱 | 포트 | 저장소 | Redis 필요 |
|----|------|--------|------------|
| [cache-caffeine-app](cache-caffeine-app/) | 8110 | Caffeine만 | 아니오 |
| [cache-redis-app](cache-redis-app/) | 8111 | Redis만 | 예 |
| [cache-caffeine-redis-app](cache-caffeine-redis-app/) | 8112 | L1 Caffeine + L2 Redis | 예 |

공통 API:

- `GET /api/products` / `GET /api/products/{id}`
- `PATCH /api/products/{id}/stock?stock=`
- `GET /api/demo/cache`
- `DELETE /api/demo/cache/products/{id}`
- L1+L2 앱만: `DELETE /api/demo/cache/l1/products/{id}`

```bash
# Caffeine만
cd cache-caffeine-app && ./gradlew bootRun

# Redis 쓰는 두 앱
docker compose up -d redis
cd cache-redis-app && ./gradlew bootRun
cd cache-caffeine-redis-app && ./gradlew bootRun
```

L1+L2 확인 순서: GET → GET(L1) → L1 evict → GET(L2, SQL 없음) → 양쪽 evict → GET(SQL).

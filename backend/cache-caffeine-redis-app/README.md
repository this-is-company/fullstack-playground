# cache-caffeine-redis-app

**L1 Caffeine + L2 Redis**. 같은 JVM 안에서는 Caffeine, 다른 인스턴스·L1 만료 후에는 Redis.

- 포트: **8112**
- Redis: `localhost:6379`

```bash
cd ../
docker compose up -d redis
cd cache-caffeine-redis-app
./gradlew bootRun
```

로그로 구분:

| 로그 | 의미 | SQL |
|------|------|-----|
| `[CACHE MISS L1+L2]` + `[MYBATIS→DB]` | 둘 다 없음 | **있음** |
| `[CACHE HIT L1-Caffeine]` | 프로세스 로컬 hit | 없음 |
| `[CACHE HIT L2-Redis]` | Redis hit, L1 재적재 | 없음 |

실험:

1. `GET /api/products/1` → MISS + SQL
2. 다시 GET → **L1 hit**, SQL 없음
3. `DELETE /api/demo/cache/l1/products/1` (Caffeine만 삭제)
4. 다시 GET → **L2 hit**, SQL 없음
5. `DELETE /api/demo/cache/products/1` (양쪽 삭제)
6. 다시 GET → SQL 다시 나옴

# cache-caffeine-redis-app

한 앱에서 저장소를 고른다. `@Cacheable` 코드는 그대로.

| 모드 | 실행 | Redis |
|------|------|--------|
| **both** (기본) | `./gradlew bootRun` | 필요 (L1+L2) |
| **caffeine** | `./gradlew bootRun --args='--spring.profiles.active=caffeine'` | 불필요 |
| **redis** | `./gradlew bootRun --args='--spring.profiles.active=redis'` | 필요 |

포트 **8112**. `GET /api/demo/cache` 의 `mode` 로 현재 저장소 확인.

## both (L1 Caffeine + L2 Redis)

```bash
cd ../ && docker compose up -d redis
cd cache-caffeine-redis-app && ./gradlew bootRun
```

| 로그 | SQL |
|------|-----|
| `[CACHE MISS L1+L2]` + `[MYBATIS→DB]` | 있음 |
| `[CACHE HIT L1-Caffeine]` | 없음 |
| `[CACHE HIT L2-Redis]` | 없음 |

1. GET `/api/products/1` → MISS + SQL  
2. 다시 GET → L1  
3. `DELETE /api/demo/cache/l1/products/1` → 다음 GET 은 L2  
4. `DELETE /api/demo/cache/products/1` → 다음 GET 은 SQL  

## caffeine만 / redis만

별도 앱(`cache-caffeine-app`, `cache-redis-app`)과 같고, 이 프로젝트에서 프로필만 바꾼 것이다.

```bash
./gradlew bootRun --args='--spring.profiles.active=caffeine'
./gradlew bootRun --args='--spring.profiles.active=redis'
```

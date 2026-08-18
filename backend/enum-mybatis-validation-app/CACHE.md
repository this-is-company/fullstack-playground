# Spring Cache — Caffeine 또는 Redis (둘 중 하나)

이 프로젝트는 **저장소를 하나만** 고른다.  
`@Cacheable` / `@CachePut` / `@CacheEvict` 코드는 그대로 두고, `spring.cache.type` 만 바꾼다.

둘 다 동시에 쓰는 방식(캐시 이름마다 다르게, L1+L2)은 나중에 필요할 때 이야기하면 된다.

---

## 공통 (이미 되어 있음)

`build.gradle.kts`:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-cache")
implementation("com.github.ben-manes.caffeine:caffeine")
implementation("org.springframework.boot:spring-boot-starter-data-redis")
```

`EnumMybatisValidationApplication` 에 `@EnableCaching`.

Service 예:

```java
@Cacheable(value = "orders", key = "#id")
public OrderResponse getById(Long id) { ... }
```

---

## Caffeine 으로 쓸 때 (기본)

인메모리. Redis 서버 불필요. 앱 재시작하면 캐시가 비워진다. 로컬·단일 인스턴스에 맞다.

### 1) `application.yml`

```yaml
spring:
  cache:
    type: caffeine
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
```

`starter-data-redis` 가 있어도 Redis 연결을 시도하지 않게 exclude 한다.

### 2) 설정 클래스

`CaffeineCacheConfig` 가 `spring.cache.type=caffeine` 일 때만 켜진다.

- 캐시 이름: `orders`, `orders-list`, `orders-search`
- TTL 10분, 최대 500건

### 3) 실행

```bash
cd enum-mybatis-validation-app
./gradlew bootRun
```

프로필을 안 주면 Caffeine 이다.

확인: `GET /api/demo/cache` → `cacheManager` 가 `CaffeineCacheManager`.  
같은 `GET /api/orders/{id}` 를 두 번 치면 두 번째는 `[MYBATIS→DB]` 로그가 없다.

---

## Redis 로 쓸 때

여러 앱 인스턴스가 캐시를 공유해야 할 때. Redis 프로세스가 떠 있어야 한다.

### 1) Redis 기동

```bash
cd backend
docker compose up -d redis
```

컨테이너 `enum-demo-redis`, 포트 **6379**.

### 2) `application-redis.yml` (이미 있음)

```yaml
spring:
  cache:
    type: redis
  autoconfigure:
    exclude: []          # Caffeine 용 Redis exclude 를 해제
  data:
    redis:
      host: localhost
      port: 6379
```

호스트/포트만 환경에 맞게 바꾸면 된다. (`SPRING_DATA_REDIS_HOST` 도 가능)

### 3) 설정 클래스

`RedisCacheConfig` 가 `spring.cache.type=redis` 일 때만 켜진다.

- 같은 캐시 이름, TTL 10분
- 값은 JSON 으로 저장

### 4) 실행 (프로필 `redis`)

```bash
cd enum-mybatis-validation-app
./gradlew bootRun --args='--spring.profiles.active=redis'
```

또는 IDE Run Configuration 에 `spring.profiles.active=redis`.

운영/개발 서버라면 환경 변수:

```bash
export SPRING_PROFILES_ACTIVE=redis
```

확인:

```bash
curl http://localhost:8093/api/demo/cache
# cacheManager → RedisCacheManager

docker exec -it enum-demo-redis redis-cli
KEYS *
# 예: orders::1
```

`GET /api/orders/{id}` 두 번 → 두 번째는 DB 로그 없음.

---

## 전환 한 줄 요약

| 하고 싶은 것 | 할 일 |
|--------------|--------|
| **Caffeine** | 아무 프로필 없이 `bootRun`. `application.yml` 의 `type: caffeine` |
| **Redis** | Redis 기동 + `--spring.profiles.active=redis` |

Service 의 `@Cacheable` 은 **수정하지 않는다.**

yml 에서 `spring.cache.type` 만 `caffeine` / `redis` 로 바꿔도 되지만, Redis 를 기본으로 두면 로컬에서 Redis 가 없으면 기동이 실패한다. 그래서 **기본 Caffeine + 필요할 때만 redis 프로필** 이 안전하다.

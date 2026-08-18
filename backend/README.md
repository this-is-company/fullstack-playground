# 웹 프로젝트에 대한 생걱
## API에 대한 응답

### response의 통일
리턴값은 항상 일치해야한다. 
오류리턴일떄도 항상 같아야한다.
다음과 비슷하면 좋겠지? error은 서버등의 통제할수 없는 오류, fail은 비지니스 오류이다.
{
    status:[success,fail,error],
    data<T>:T,
    errorCode:String,
    errorMessage:String
}

컨트롤러에서 service만 리턴해도 일괄적으로 나오도록 리턴할때 컷해서 response를 만든다.

### Restful 방식으로 진행한다.
/서비스/

- 시작은 "/"로 시작하며, 뒤에는 "/"을 붙이지 않는다.
- GET, PUT, PATCH, DELETE, POST를 사용
- 목록에 대한 조회 POST로 한다. 
- 상세조회는 GET을 쓸수 있으며 상세조회에 대한 행동을 넣을수 있다.
- 등록/전체수정은 PUT을, 부분 수정은 PATCH를 한다. 
- Mapping은 통일되게 나오도록 한다.


### paging 전략
page를 위한 request로 받아야할것, response로 내보낼 것
DB에서 어떻게 처리할것인가

count는 어떻게 할것인가

### exception 전략
사용자는 비지니스 오류만 낼수 있다. 
비지니스오류는 enum으로 코드화한다.

### 문서화
swagger로 한다.
1. 성공했을때의 리턴값
2. (비지니스오류에대한) exception일때의 리턴값
3. 접근할수 없는 서버오류 또는 404등, 처리되기 전에 나오는 오류, validation오류도 마찬가지
4. javadoc는 스웨거문서로 거의 대체되며, service부분에서만 나오게 한다.

#### swagger에 대한 어노테이션
1. 컨트롤러 클래스에서는 Tag를 쓰며, 일반적인 Resultful 메서드에는 @Operation을 달지 않는다.
   - 추가적인 API 메서드에서 @Operation으로 사례를 단다.
2. Dto에서는 스키마 어노테이션을 쓴다.


#### 개인적인 바람
공통적으로 나오는 오류는 dto에서 나왔으면 좋겠다.






# DB Query Lib → Nexus → Consumer App

로컬 Nexus에 DB 조회 라이브러리를 배포하고, 새 프로젝트에서 그 아티팩트를 받아 DB 조회를 검증하는 샘플입니다.

구조·동작 상세 설명은 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)를 보세요.

## 구성

| 경로 | 역할 |
|------|------|
| `api-common/` | 여러 앱 공유 API 경로 상수 (`ApiPaths`) |
| `db-query-lib/` | JDBC 조회 라이브러리 (`DbQueryService`) |
| `db-query-mybatis-lib/` | MyBatis 조회 라이브러리 (`MybatisDbQueryService`) |
| `consumer-app/` | JDBC lib 소비자 (Maven, :8080) |
| `consumer-gradle-app/` | JDBC lib 소비자 (Gradle, :8082) |
| `consumer-mybatis-app/` | MyBatis lib 소비자 (Maven, :8083) |
| `consumer-mybatis-gradle-app/` | MyBatis lib 소비자 (Gradle, :8084) |
| `security-form-app/` | Spring Security + DB `app_users` 폼 로그인 (:8090) |
| `security-sso-app/` | Keycloak OAuth2 로그인 + JWT API 검증 (Maven, :8091) |
| `security-sso-gradle-app/` | Keycloak OAuth2 로그인 + JWT API 검증 (Gradle, :8092) |
| `enum-mybatis-validation-app/` | CodeEnum + MyBatis TypeHandler + Validation groups + **Spring Cache** (:8093) |
| `enum-jpa-validation-app/` | CodeEnum + Spring Data JPA Specification (:8094) |
| `enum-jpa-querydsl-app/` | CodeEnum + JPA + QueryDSL ConditionBuilder (:8095) |
| `enum-jpa-querydsl-kotlin-app/` | 위 QueryDSL 앱 Kotlin 버전 (:8096) |
| `enum-sqlalchemy-python-app/` | FastAPI + SQLAlchemy ConditionBuilder (Python, :8097) |
| `cache-caffeine-app/` | Caffeine 전용 캐시 + MyBatis SQL 로그 (:8110) |
| `cache-redis-app/` | Redis 전용 캐시 + MyBatis SQL 로그 (:8111) |
| `cache-caffeine-redis-app/` | L1 Caffeine + L2 Redis + MyBatis SQL 로그 (:8112) |
| `api-mybatis-app/` | MyBatis + Swagger + 공통응답 `{status,data,error}` (:8130) |
| `api-jpa-app/` | JPA + Swagger + 공통응답 (:8131) |
| `api-querydsl-app/` | QueryDSL + Swagger + 공통응답 (:8132) |
| `api-mybatis-app/` | MyBatis + Swagger + 공통응답 `{status,data,error}` (:8130) |
| `api-jpa-app/` | JPA + Swagger + 공통응답 (:8131) |
| `api-querydsl-app/` | QueryDSL + Swagger + 공통응답 (:8132) |
| `docker-compose.yml` | Nexus(8081) + PostgreSQL(5432) + Keycloak(8180) + Redis(6379) |
| `scripts/` | 인프라 기동 / 배포 / 실행 |

## 단계별 실행

```bash
chmod +x scripts/*.sh

# 1) Nexus + Postgres 기동
./scripts/start-infra.sh

# 2-A) JDBC 라이브러리 배포
./scripts/publish.sh

# 2-B) MyBatis 라이브러리 배포
./scripts/publish-mybatis.sh

# 2-C) 공유 API 경로 상수 배포
./scripts/publish-api-common.sh

# 3) 소비자 기동
./scripts/run-consumer.sh                 # Maven JDBC   :8080
./scripts/run-gradle-consumer.sh          # Gradle JDBC  :8082
./scripts/run-mybatis-consumer.sh         # Maven MyBatis :8083
./scripts/run-mybatis-gradle-consumer.sh  # Gradle MyBatis:8084

# 4) Security 샘플
docker compose up -d keycloak
./scripts/run-security-form.sh            # DB 폼 로그인 :8090
./scripts/run-security-sso.sh             # Keycloak SSO Maven  :8091
./scripts/run-security-sso-gradle.sh      # Keycloak SSO Gradle :8092
```

보안 샘플 상세: [docs/SECURITY.md](docs/SECURITY.md)

## 조회 API

### JDBC Maven (`:8080`) / Gradle (`:8082`)
- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/users/count`
- `GET /api/query?sql=SELECT%20*%20FROM%20users`

### MyBatis Maven (`:8083`) / Gradle (`:8084`)
- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/users/count`
- `GET /api/users/by-department?department=Engineering`

## Nexus

- UI: http://localhost:8081
- 계정: `admin` / `.nexus-password` 참고
- JDBC: `com.example:db-query-lib:1.0.2`
- MyBatis: `com.example:db-query-mybatis-lib:1.0.0`
- API paths: `com.example:api-common:1.0.0`

## Spring Cache (`enum-mybatis-validation-app`)

`:8093` — Controller → Service → **OrderQueryService(@Cacheable)** → OrderMapper → DB

| 캐시 | 키 | 적용 API / 메서드 |
|------|-----|-------------------|
| `orders` | `#id` | `GET /api/orders/{id}`, `GET /api/resource/orders/{id}` → `OrderQueryService.getById` |
| `orders-list` | `'all'` | `POST /api/orders/search` `{}`, `POST /api/resource/orders` (조건 없음) → `findAll` |
| `orders-search` | `#criteria.cacheKey()` | `POST /api/orders/search` (조건 있음) → `searchFlexible` |

쓰기 시 `OrderService`에서 `@CachePut` / `@CacheEvict` (create·update·cancel·delete).

명시 예제 API (`CacheDemoController`):

| 어노테이션 | API | 효과 |
|-----------|-----|------|
| `@Cacheable` | `GET /api/orders/{id}` | hit 이면 DB 생략 |
| `@CachePut` | `PUT /api/demo/cache/orders/{id}?customerName=` | 항상 실행 + 반환값으로 캐시 덮어씀 |
| `@CacheEvict` | `DELETE /api/demo/cache/orders/{id}` | 해당 키만 삭제 (DB 행은 유지) |

의존성: `spring-boot-starter-cache` + `caffeine` + `starter-data-redis`.  
기본은 Caffeine, Redis 로 바꾸려면 `redis` 프로필. 상세: [enum-mybatis-validation-app/CACHE.md](enum-mybatis-validation-app/CACHE.md)

확인:
- 로그: `[CACHE→DB]`, `[MYBATIS→DB]` — 캐시 hit 시 둘 다 없음
- `GET /api/demo/cache` — 캐시 통계·흐름 설명

```bash
cd enum-mybatis-validation-app && ./gradlew bootRun
# 같은 id로 GET 두 번 → 두 번째는 DB 로그 없음
curl http://localhost:8093/api/demo/cache
curl -X PUT "http://localhost:8093/api/demo/cache/orders/1?customerName=After-Put"
curl -X DELETE http://localhost:8093/api/demo/cache/orders/1
```

Caffeine / Redis / L1+L2 를 **앱을 나눠** 보는 샘플: [CACHE-APPS.md](CACHE-APPS.md)

MyBatis / JPA / QueryDSL 조회 + 공통 응답 샘플: [API-DEMO-APPS.md](API-DEMO-APPS.md)

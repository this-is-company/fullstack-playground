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
| `enum-mybatis-validation-app/` | CodeEnum + MyBatis TypeHandler + Validation groups (:8093) |
| `docker-compose.yml` | Nexus(8081) + PostgreSQL(5432) + Keycloak(8180) |
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

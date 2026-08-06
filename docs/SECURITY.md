# Spring Security 샘플

두 가지 인증 방식을 비교하는 샘플입니다.

## 1) `security-form-app` — DB 사용자 테이블 로그인

- Spring Security Form Login
- PostgreSQL `app_users` 테이블 (JPA `ddl-auto=update`로 자동 생성)
- 비밀번호 BCrypt 저장
- 포트: **8090**

```bash
# Postgres가 떠 있어야 함
./scripts/run-security-form.sh
```

| 계정 | 비밀번호 | 역할 |
|------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

브라우저: http://localhost:8090/login  
Swagger UI (로그인 불필요): http://localhost:8090/swagger-ui.html

API (Basic Auth도 가능):

```bash
curl -u user:user123 http://localhost:8090/api/me
curl -u admin:admin123 http://localhost:8090/api/admin/ping
```

토큰을 직접 만들지 않고 **세션(쿠키)** 또는 **HTTP Basic**으로 API를 호출합니다.

---

## 2) `security-sso-app` / `security-sso-gradle-app` — Keycloak SSO + JWT API 검증

- Keycloak (Docker `:8180`)이 로그인/토큰 발급
- Spring Security OAuth2 Login (브라우저 SSO)
- Spring Security OAuth2 Resource Server (Bearer JWT 검증)
- Maven 포트: **8091** / Gradle 포트: **8092**

```bash
docker compose up -d keycloak
./scripts/run-security-sso.sh          # Maven  :8091
./scripts/run-security-sso-gradle.sh   # Gradle :8092
```

| Keycloak 계정 | 비밀번호 | realm role |
|---------------|----------|------------|
| demo | demo123 | user |
| adminuser | admin123 | user, admin |

브라우저 SSO:
- Maven: http://localhost:8091 → `Keycloak으로 로그인`
- Gradle: http://localhost:8092 → `Keycloak으로 로그인`

Swagger UI (로그인 불필요):
- http://localhost:8091/swagger-ui.html
- http://localhost:8092/swagger-ui.html

API 토큰 검증:

```bash
./scripts/test-sso-api.sh
./scripts/test-sso-api.sh http://localhost:8092/api/me
```

토큰은 **Keycloak이 생성**합니다. 앱은 JWT 서명/issuer만 검증합니다.

Keycloak 관리콘솔: http://localhost:8180 (admin / admin123)
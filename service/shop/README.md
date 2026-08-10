# shop — 미니 커머스 서비스

`service/` 아래 **첫 번째 풀스택 샘플**. 나중에 다른 서비스도 같은 방식으로 `service/<이름>/`에 추가하면 됩니다.

## 구성

| 경로 | 역할 | 포트 |
|------|------|------|
| `sso/` | JWT 발급 (로그인) | **8100** |
| `catalog-api/` | 상품 API (**인증 없음**) | **8101** |
| `order-api/` | 주문 API (**JWT**, catalog 호출) | **8102** |
| `shop-web/` | 일반 사용자 프론트 (Vite+React+TanStack Query) | **5173** |
| `admin-web/` | 관리 프론트 | **5174** |

```
shop-web / admin-web
        │
        ├─► sso        :8100  로그인 → JWT
        ├─► catalog    :8101  상품 (비인증)
        └─► order      :8102  주문 (JWT) ──► catalog
```

## 인증 / Swagger

| 환경 | order-api |
|------|-----------|
| **local** (기본) | Swagger·API `permitAll` — Swagger에서 바로 Try it out |
| **dev** | `/api/**` JWT 필수 — `Bearer` 토큰으로 호출 |

```bash
# 로컬 (Swagger 편하게)
cd order-api && ./gradlew bootRun

# 개발 서버처럼 인증 강제
cd order-api && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 데모 계정

| 사용자 | 비밀번호 | 역할 |
|--------|----------|------|
| `admin` | `admin123` | ADMIN, USER |
| `shopper` | `shopper123` | USER |

## 실행

```bash
# 터미널 3개 — 백엔드
./scripts/run-sso.sh
./scripts/run-catalog.sh
./scripts/run-order.sh

# 프론트
cd shop-web && npm install && npm run dev
cd admin-web && npm install && npm run dev
```

또는:

```bash
./scripts/run-backends.sh   # sso + catalog + order 백그라운드
```

### URL

- Shop: http://localhost:5173  
- Admin: http://localhost:5174  
- SSO Swagger: http://localhost:8100/swagger-ui.html  
- Catalog Swagger: http://localhost:8101/swagger-ui.html  
- Order Swagger: http://localhost:8102/swagger-ui.html  

## 다음 서비스

같은 패턴으로 예:

```
service/
  shop/          ← 현재
  booking/       ← 나중에
  cms/
```

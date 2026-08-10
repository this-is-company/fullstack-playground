# test

샘플 모노레포입니다.

| 경로 | 설명 |
|------|------|
| [backend/](backend/) | Java/Kotlin/Python 백엔드·인프라 샘플 (Nexus/Postgres/Keycloak) |
| [service/](service/) | 프론트+백엔드가 묶인 **서비스** 샘플 (첫 번째: [shop](service/shop/)) |

```bash
# 인프라 (backend 샘플용)
cd backend && ./scripts/start-infra.sh

# 미니 커머스 서비스
cd service/shop
./scripts/run-backends.sh
cd shop-web && npm run dev    # :5173
cd admin-web && npm run dev   # :5174
```

### Python (참고만 — 지금은 추가하지 않음)

`service/` 메인은 Java + React로 두고, 파이썬은 겹치지 않는 옆에 두는 쪽을 추천.

| 추천 | 예 |
|------|----|
| 배치·워커 | 고아 파일 정리, 리포트 cron |
| 집계·분석 API | 매출/인기상품 → admin 대시보드 |
| 운영 스크립트 | 시드·카탈로그/주문 API 호출 도구 |
| 학습 샘플 | [backend/enum-sqlalchemy-python-app](backend/enum-sqlalchemy-python-app/) (패턴 비교용) |

비추천: shop의 SSO/catalog/order를 파이썬으로 다시 만들기 (역할 중복).

### Django vs Java + React (참고)

처음부터라면 **Django도 괜찮은 선택**이다. 다만 이 모노레포처럼 SSO / 비인증 API / 인증 API / 관리·쇼핑 프론트를 나눠 연습하려면 **Java + React**가 목적에 더 잘 맞는다.

| | Django | Java + React (현재 shop) |
|--|--------|---------------------------|
| 좋은 점 | 모델·Admin·ORM·인증이 한곳에 있어 빨리 완성 / 소수 팀에 유리 | 역할 경계·Swagger·이중 SPA·실무 조합 연습에 유리 |
| 아쉬운 점 | 프론트 분리·다중 API 쪼개면 “올인원” 장점이 줄어듦 | 초기 세팅·CORS·토큰·빌드 비용이 큼 |

| 목표 | 추천 |
|------|------|
| 쇼핑몰 하나를 빨리 완성 | Django (Admin + 템플릿, 또는 DRF + React 하나) |
| SSO·다중 API·이중 프론트·Swagger 연습 | Java + React (현재 구조) |
| Django를 쓰고 싶을 때 | 모놀리스 shop을 따로 두거나, 집계/보조 API에만 쓰기 |

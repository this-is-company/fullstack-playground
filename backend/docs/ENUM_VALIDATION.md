# Enum + MyBatis + Validation 샘플

`enum-mybatis-validation-app` (Gradle, `:8093`)

## 구성

| 구성요소 | 설명 |
|----------|------|
| `CodeEnum` | code/description 을 가진 Enum 추상화 |
| `CodeEnumTypeHandler` | DB code ↔ Enum 변환 |
| `CodeEnumMyBatisConfig` | `CodeEnum` 구현 Enum 전부 TypeHandler 자동 등록 |
| `CodeEnumJacksonConfig` | Request/Response JSON code ↔ Enum 변환 (null/blank → null) |
| Enum 3종 | `OrderStatus`, `PayMethod`, `UserGrade` |
| Validation groups | `Create` / `Update` 규칙 분리 |
| `@NotBlankFieldsInList` | list null/empty 및 요소 필드 null/blank 검증 |

## 실행

```bash
./scripts/run-enum-validation.sh
# 또는
cd enum-mybatis-validation-app && ./gradlew bootRun
```

## 테스트

```bash
cd enum-mybatis-validation-app && ./gradlew test
```

검증 포인트:
- Enum code null/blank → null 변환
- Create 시 status/payMethod 필수, Update 시 선택
- items list 자체가 null → 400
- list 요소의 productName/sku 가 null 또는 `""` → 400
- Create 에 id 포함 → 400 / Update 는 status 없어도 validation 통과

## API 예

```bash
curl -s http://localhost:8093/api/orders -H 'Content-Type: application/json' -d '{
  "customerName":"Kim",
  "status":"P",
  "payMethod":"CARD",
  "userGrade":"G",
  "items":[{"productName":"Book","sku":"B-1","quantity":1}]
}'
```

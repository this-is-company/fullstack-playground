# 구조 및 동작 설명

DB 조회 로직을 **재사용 라이브러리**로 만들고, **Nexus**에 배포한 뒤, **새 Spring Boot 프로젝트**에서 그 라이브러리를 받아 DB를 조회하는 샘플의 설계·구조·런타임 흐름 문서입니다.

---

## 1. 무엇을 만든 것인가

목표 흐름은 아래와 같습니다.

1. DB 조회 기능을 `db-query-lib` 라이브러리로 분리한다.
2. Docker로 띄운 Nexus에 jar + sources를 배포한다.
3. 별도 프로젝트 `consumer-app`이 Nexus에서 의존성을 받아 기동한다.
4. `consumer-app` API로 PostgreSQL 데이터를 조회한다.

즉, **라이브러리 제작 → 사내/로컬 저장소 배포 → 다른 프로젝트에서 소비** 패턴을 로컬에서 재현한 것입니다.

```text
[개발자]
   │
   │  mvn deploy
   ▼
┌──────────────┐     docker      ┌──────────────┐
│ db-query-lib │ ──────────────► │    Nexus     │
│  (라이브러리) │                 │   :8081      │
└──────────────┘                 └──────┬───────┘
                                        │
                              mvn resolve (의존성)
                                        │
                                        ▼
                               ┌────────────────┐
                               │  consumer-app  │
                               │   Spring Boot  │
                               │     :8080      │
                               └───────┬────────┘
                                       │ JDBC
                                       ▼
                               ┌────────────────┐
                               │  PostgreSQL    │
                               │     :5432      │
                               └────────────────┘
```

---

## 2. 디렉터리 구조

```text
test/
├── docker-compose.yml          # Nexus + PostgreSQL 인프라
├── settings.xml                # Maven → Nexus 접속 설정
├── README.md                   # 빠른 실행 안내
├── docs/
│   └── ARCHITECTURE.md         # 이 문서
├── scripts/
│   ├── start-infra.sh          # Docker 인프라 기동 + Nexus 비밀번호 초기화
│   ├── setup-nexus.sh          # Nexus admin 비밀번호 설정
│   ├── publish.sh              # db-query-lib 빌드 및 Nexus 배포
│   ├── run-consumer.sh         # consumer-app 기동 (Nexus에서 lib resolve)
│   └── demo.sh                 # 위 과정을 한 번에 실행 + API 호출
├── infra/
│   └── postgres/
│       └── init.sql            # users 테이블 + 샘플 데이터
├── db-query-lib/               # 배포용 DB 조회 라이브러리
│   ├── pom.xml
│   └── src/main/java/com/example/dbquery/
│       ├── DbQueryService.java
│       ├── model/QueryResult.java
│       └── autoconfigure/DbQueryAutoConfiguration.java
└── consumer-app/               # Nexus에서 lib를 받아 쓰는 새 프로젝트
    ├── pom.xml
    └── src/main/java/com/example/consumer/
        ├── ConsumerApplication.java
        └── QueryController.java
```

| 구성 요소 | 역할 |
|-----------|------|
| `db-query-lib` | 다른 프로젝트가 가져다 쓰는 **공통 DB 조회 서비스** |
| `consumer-app` | 라이브러리를 **소비**하는 예시 애플리케이션 (Maven, :8080) |
| `consumer-gradle-app` | 라이브러리를 **소비**하는 예시 애플리케이션 (Gradle, :8082) |
| Nexus | Maven 아티팩트 저장소 (jar / sources / javadoc) |
| PostgreSQL | 실제 조회 대상 DB |
| `scripts/` | 수동 작업을 줄이는 운영 스크립트 |

---

## 3. 각 컴포넌트 상세

### 3.1 `db-query-lib` (라이브러리)

Spring Boot 앱이 아니라 **jar 라이브러리**입니다.  
핵심은 `DbQueryService` 입니다.

| 메서드 | 설명 |
|--------|------|
| `findAll(table)` | 테이블 전체 조회 |
| `findById(table, idColumn, id)` | PK 단건 조회 |
| `query(sql, args...)` | SELECT/WITH만 허용하는 임의 조회 |
| `queryNamed(sql, params)` | Named parameter 조회 |
| `queryAsResult(sql, ...)` | `count + rows` 형태로 반환 |
| `count(table)` | 건수 조회 |

보안/안전 장치:

- `query` 계열은 **SELECT / WITH만** 허용
- 테이블명·컬럼명은 identifier 정규식으로 검증 (SQL injection 완화)

Spring Boot AutoConfiguration:

- `DbQueryAutoConfiguration`이 `JdbcTemplate` 빈이 있을 때 `DbQueryService`를 자동 등록
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 등록
- `JdbcTemplateAutoConfiguration` **이후**에 실행되도록 순서 지정  
  (이 순서가 어긋나면 consumer에서 빈을 못 찾는 오류가 납니다)

배포 산출물 (Nexus):

- `db-query-lib-1.0.1.jar`
- `db-query-lib-1.0.1-sources.jar` ← IDE에서 소스 보기용
- `db-query-lib-1.0.1-javadoc.jar`

Maven 좌표:

```text
com.example:db-query-lib:1.0.1
```

### 3.2 `consumer-app` (소비자 프로젝트)

Nexus에 올라간 라이브러리를 **의존성으로만** 받아 쓰는 새 Spring Boot 앱입니다.

- `pom.xml`에 `db-query-lib` dependency 선언
- repository를 `http://localhost:8081/repository/maven-public/` 로 지정
- `QueryController`가 `DbQueryService`를 주입받아 REST API 제공
- `application.yml`로 PostgreSQL 접속 정보 설정

소비자 입장에서는 DB 조회 구현을 직접 쓰지 않고, 라이브러리 API만 호출합니다.

### 3.3 Docker 인프라

`docker-compose.yml`이 두 서비스를 띄웁니다.

| 서비스 | 포트 | 용도 |
|--------|------|------|
| `nexus` (`sonatype/nexus3`) | 8081 | Maven 저장소 UI/API |
| `postgres` (`postgres:16-alpine`) | 5432 | 샘플 DB (`demodb`) |

Postgres 초기 데이터는 `infra/postgres/init.sql`에서 `users` 테이블과 5건의 샘플 row를 넣습니다.

### 3.4 Maven `settings.xml`

로컬 Maven이 Nexus를 쓰도록 설정합니다.

- `servers`: Nexus 배포용 계정 (`admin` + `NEXUS_PASSWORD`)
- `mirrors`: 의존성 resolve를 Nexus `maven-public`으로 보냄
- HTTP blocker 무력화: Maven 3.8+가 localhost HTTP를 막는 문제 회피

---

## 4. 런타임 동작 흐름

### 4.1 인프라 기동

```bash
./scripts/start-infra.sh
```

1. `docker compose up -d`로 Nexus / Postgres 시작
2. Postgres health check 대기
3. Nexus status API 대기 (첫 기동은 1~2분 걸릴 수 있음)
4. `setup-nexus.sh`가 초기 admin 비밀번호를 읽고 변경 후 `.nexus-password`에 저장

### 4.2 라이브러리 배포

```bash
./scripts/publish.sh
```

1. `db-query-lib`를 `mvn clean deploy`
2. jar / sources / javadoc 생성
3. Nexus `maven-releases` 저장소로 업로드

배포 확인:

- Nexus UI: http://localhost:8081 → Browse → `maven-releases`
- 경로 예: `com/example/db-query-lib/1.0.1/`

### 4.3 소비자 앱 기동

```bash
./scripts/run-consumer.sh
```

1. 로컬 `.m2`에 캐시된 `db-query-lib`를 지워 **Nexus에서 다시 받도록** 강제
2. `consumer-app`을 `spring-boot:run`
3. Maven이 Nexus에서 `db-query-lib:1.0.1` resolve
4. Spring Boot 기동 시 AutoConfiguration으로 `DbQueryService` 빈 생성
5. Tomcat이 `:8080`에서 API 제공

요청 처리 경로:

```text
HTTP GET /api/users
   → QueryController
   → DbQueryService.findAll("users")   ← Nexus에서 받은 라이브러리
   → JdbcTemplate
   → PostgreSQL users 테이블
   → JSON 응답
```

### 4.4 API 목록

| Method | Path | 동작 |
|--------|------|------|
| GET | `/api/users` | 전체 사용자 조회 |
| GET | `/api/users/{id}` | 단건 조회 |
| GET | `/api/users/count` | 건수 |
| GET | `/api/query?sql=...` | 임의 SELECT 조회 |

예시:

```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1
curl "http://localhost:8080/api/query?sql=SELECT%20name,%20department%20FROM%20users"
```

---

## 5. 왜 이렇게 나눴는가

| 선택 | 이유 |
|------|------|
| 라이브러리 / 앱 분리 | 실제 조직에서 공통 모듈을 배포·재사용하는 방식을 그대로 연습 |
| Nexus | Maven 아티팩트 사내 배포의 대표 저장소 |
| sources jar 함께 배포 | 소비자 IDE에서 라이브러리 소스를 열람 가능 |
| Spring Boot AutoConfiguration | consumer에 별도 `@Bean` 선언 없이 바로 주입 가능 |
| Docker Compose | Nexus/DB를 로컬에 빠르게 재현 |

---

## 6. 로그 보는 위치

| 대상 | 방법 |
|------|------|
| consumer-app | `tail -f consumer-app.log` |
| Nexus / Postgres | `docker compose logs -f` |
| Nexus만 | `docker compose logs -f nexus` |
| Postgres만 | `docker compose logs -f postgres` |

---

## 7. 접속 정보 요약

| 항목 | 값 |
|------|-----|
| consumer API | http://localhost:8080 |
| Nexus UI | http://localhost:8081 |
| Nexus 계정 | `admin` / `.nexus-password` 참고 |
| Postgres | `localhost:5432` / DB `demodb` / user `demo` / password `demo123` |
| 라이브러리 좌표 | `com.example:db-query-lib:1.0.1` |

---

## 8. 자주 겪는 이슈

**`DbQueryService` 빈을 못 찾음**  
→ AutoConfiguration이 `JdbcTemplate`보다 먼저 실행된 경우.  
현재는 `after = JdbcTemplateAutoConfiguration.class`로 맞춰 두었습니다.

**Release 버전 재배포 실패**  
→ Nexus `maven-releases`는 같은 버전 덮어쓰기를 막습니다.  
수정 시 `1.0.2`처럼 버전을 올리세요.

**Maven HTTP 차단**  
→ Maven 3.8+ 기본 HTTP blocker 때문입니다.  
`settings.xml`에서 blocker를 무력화해 두었습니다.

**Nexus 첫 기동이 느림**  
→ 정상입니다. `start-infra.sh`가 ready 상태를 기다립니다.

---

## 9. 한 줄 요약

> **DB 조회 서비스(`DbQueryService`)를 Spring Boot 라이브러리로 만들고 Nexus에 올린 다음, 새 프로젝트(`consumer-app`)가 그 아티팩트를 받아 PostgreSQL을 조회한다.**

---

## 10. 실험: DB 접속정보를 lib에 넣기 (`1.0.2`)

`1.0.2`부터 `db-query-lib`가 기본 DataSource 접속정보를 내장합니다.

- 기본값: `localhost:5432/demodb`, user `demo`, password `demo123`
- consumer의 `spring.datasource` 설정 없이도 동작
- 덮어쓰기: `db-query.datasource.url` / `username` / `password`
- PostgreSQL 드라이버도 lib 의존성으로 포함

학습용 데모이며, 실제 환경에서는 비밀번호가 jar에 들어가므로 권장하지 않습니다.
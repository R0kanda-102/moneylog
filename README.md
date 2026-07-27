# 머니로그 (MoneyLog)

개인이 수입/지출을 기록하고 카테고리별·월별 통계로 한눈에 확인하는 개인 가계부 웹 서비스입니다.

부트캠프 5일 과정(요구사항 정의 → 도메인 설계 → CRUD 구현 → 인증/인가 → 프론트엔드 → Docker/CI-CD/EC2 배포)을 통해 Java/Spring/JPA/Security/프론트/DevOps 전 과정을 복습하고, AWS EC2 실배포까지 완주하는 것을 목표로 진행했습니다.

- 배포 URL: http://15.165.159.242:8080
- GitHub: https://github.com/R0kanda-102/moneylog
- 상세 문서: [요구사항 정의서](docs/requirements.md) · [ERD](docs/erd.md) · [API 명세서](docs/api-spec.md)

## 주요 기능

- **회원가입/로그인** — 이메일/비밀번호 기반, BCrypt 해시 저장, JWT 발급
- **카테고리 관리** — 회원가입 시 기본 카테고리(식비/교통/주거/문화/급여/용돈) 자동 시드 + 직접 추가/수정/삭제
- **거래내역 CRUD** — 수입/지출 등록·조회·수정·삭제
- **목록 필터 + 페이징** — 월(yearMonth) · 타입(INCOME/EXPENSE) · 카테고리로 필터링, page/size 페이징
- **월별 통계** — 이번 달 총수입 · 총지출 · 잔액, 카테고리별 지출 집계
- **인가** — 모든 조회/수정/삭제는 본인 데이터만 접근 가능 (JWT 기반, 소유권 검증)
- **입력 검증 + 표준 에러 응답** — `{ success, code, message, data }` 형태로 통일된 응답 봉투
- **프론트엔드** — 로그인 화면, 거래내역 목록+등록 화면
- **API 문서화** — Swagger UI
- **배포 자동화** — Docker + GitHub Actions(CI/CD) + AWS EC2

## 기술 스택

| 구분 | 스택 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 (Web MVC, Data JPA, Security, Validation) |
| Build | Gradle |
| DB | H2 (로컬 개발) / MySQL 8 (운영) |
| 인증 | Spring Security + JWT (jjwt 0.12.6) + BCrypt |
| API 문서 | springdoc-openapi-starter-webmvc-ui 3.0.3 (Swagger UI) |
| 기타 | Lombok |
| 프론트엔드 | HTML/CSS/JS (Vanilla, Spring Boot 정적 리소스) |
| 인프라 | Docker(멀티스테이지 빌드), Docker Compose, GitHub Actions(CI/CD), AWS EC2, GHCR |

## 실행 방법

### 1) 로컬 실행 (H2 인메모리 DB)

```bash
./gradlew bootRun
```

- 기본 프로파일은 `local`이며, 애플리케이션 실행 시 H2 인메모리 DB가 자동 구성됩니다.
- 접속: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:moneylog`)
- 계정은 `/api/auth/signup`으로 직접 생성해서 테스트합니다.

### 2) Docker Compose 실행 (MySQL 8 연동)

프로젝트 루트에 `.env` 파일을 만들고 아래 값을 채웁니다.

```env
DB_ROOT_PASSWORD=your-root-password
DB_PASSWORD=your-app-password
JWT_SECRET=your-jwt-secret-base64
```

```bash
docker compose up -d --build
```

- `app` 컨테이너는 `prod` 프로파일로 기동되며 `db`(MySQL 8) 컨테이너에 연결됩니다.
- 접속: http://localhost:8080

## API 요약

전체 명세는 [docs/api-spec.md](docs/api-spec.md)를 참고하세요. 서버 실행 후 Swagger UI에서 직접 확인·테스트할 수 있습니다.

- 로컬: http://localhost:8080/swagger-ui.html
- 배포: http://15.165.159.242:8080/swagger-ui.html

| 영역 | 메서드/경로 | 설명 | 인증 |
|---|---|---|:--:|
| 인증 | `POST /api/auth/signup` | 회원가입 | ❌ |
| 인증 | `POST /api/auth/login` | 로그인 (JWT 발급) | ❌ |
| 카테고리 | `GET/POST /api/categories`, `PUT/DELETE /api/categories/{id}` | 카테고리 조회/등록/수정/삭제 | ✅ |
| 거래내역 | `GET/POST /api/transactions`, `GET/PUT/DELETE /api/transactions/{id}` | 거래 조회/등록/수정/삭제 | ✅ |
| 통계 | `GET /api/statistics/monthly?yearMonth=` | 월별 총수입/지출/잔액 + 카테고리별 지출 | ✅ |

인증이 필요한 API는 요청 헤더에 `Authorization: Bearer {accessToken}`을 포함해야 합니다.

## 배포

- URL: http://15.165.159.242:8080
- 테스트 계정: `test@moneylog.com` / `Test1234!`
- 배포 방식: GitHub Actions가 `main` 브랜치 푸시 시 Docker 이미지를 빌드해 GHCR에 push하고, EC2에 SSH로 접속해 `docker compose pull && up -d`로 재배포합니다.

## ERD 요약

핵심 엔티티는 User(사용자) · Category(카테고리) · Transaction(거래내역) 3개이며, 모두 1:N 관계입니다. 상세 컬럼/제약/DDL은 [docs/erd.md](docs/erd.md)를 참고하세요.

```
User 1 ── N Category      (사용자당 여러 카테고리)
User 1 ── N Transaction   (사용자당 여러 거래)
Category 1 ── N Transaction (카테고리당 여러 거래)
```

- 금액은 `BIGINT`(원 단위 정수)로 저장 — 원화는 소수점이 없어 부동소수 오차 위험을 피함
- `type`(INCOME/EXPENSE)은 `ENUM` + JPA `EnumType.STRING`으로 저장 — 순서 변경으로 인한 데이터 훼손 방지
- 삭제 정책은 hard delete — 개인 가계부 특성상 삭제 이력 추적 요구 없음
- (도전 과제) 카테고리별 월 예산(Budget) 엔티티는 이번 범위에서는 미구현

## 회고 (KPT)

### Keep

- 요구사항 정의 → ERD → API 명세를 먼저 문서로 정리하고 나서 구현에 들어간 순서가 잘 맞았다. 덕분에 엔드포인트/응답 형식을 중간에 갈아엎는 일 없이 진행할 수 있었다.
- 공통 응답 봉투(`{success, code, message, data}`)와 `ErrorCode` enum을 초반에 정해두니 컨트롤러/예외 처리 코드가 일관되게 유지됐다.
- Docker 멀티스테이지 빌드 + docker-compose로 로컬에서 운영 환경(MySQL)과 동일하게 검증하고 배포한 것이 EC2에서의 삽질을 줄여줬다.

### Problem

- Spring Boot 4.1.0 버전대에서 Gradle/의존성 버전 조합을 맞추는 데 예상보다 시간이 걸렸다. (버전 호환 이슈)
- Docker로 로컬 실행 시 이미 사용 중인 포트와 충돌해서 컨테이너가 뜨지 않는 문제가 있었다.
- GHCR에 이미지를 푸시할 때 권한 문제와, 이미지 태그에 대문자가 섞여 들어가서 push가 실패하는 문제를 겪었다. (GHCR 이미지 태그는 소문자만 허용)
- 배포 도중 EC2 인스턴스에 SSH 접속이 안 되는 상황이 발생해서, 원인을 찾기보다 인스턴스를 새로 생성하는 쪽으로 해결했다.
- t3.micro 인스턴스의 메모리가 부족해서 애플리케이션이 기동 중 죽는 문제가 있었고, 스왑 메모리 2GB를 추가해서 해결했다.

### Try

- 다음에는 프로젝트 초반에 Spring Boot/Gradle/Java 버전 조합을 먼저 확정하고 시작해서, 중간에 버전 때문에 멈추는 시간을 줄인다.
- Docker/EC2 관련 트러블슈팅(포트 충돌, GHCR 태그 규칙, 인스턴스 메모리/스왑 설정)을 겪을 때마다 바로 기록해두는 별도 트러블슈팅 문서를 만들어, 다음 프로젝트에서 같은 문제를 반복하지 않는다.
- EC2 인스턴스 접속 불가 같은 문제는 재생성 전에 로그/보안그룹/키페어를 먼저 점검하는 체크리스트를 만들어 원인을 파악한 뒤 대응한다.
- t3.micro처럼 메모리가 작은 인스턴스를 쓸 때는 배포 전에 스왑 설정을 기본 셋업 스크립트에 포함시켜 둔다.

# 머니로그(MoneyLog) ERD / 테이블 정의서

- 작성자: (이름)
- 작성일: 2026-07-23
- 관련 문서: [docs/requirements.md](./requirements.md)

## 1. 엔티티 도출

요구사항 정의서의 유저 스토리에서 뽑은 명사(저장 대상)는 4개입니다.

| 엔티티 | 근거 유저 스토리 | 비고 |
|--------|------------------|------|
| User | US-1, US-7 | 로그인 주체이자 모든 데이터의 소유자 |
| Category | US-4 | 사용자가 직접 추가/수정/삭제 가능 → 반드시 테이블 |
| Transaction | US-2, US-3, US-5, US-6 | 머니로그의 핵심 엔티티 |
| Budget (도전) | US-8 | 기본 과제 범위에서는 생략 가능 |

> type(수입/지출)은 값이 2개로 고정되어 바뀌지 않으므로 별도 테이블이 아닌 ENUM 속성으로 처리한다.

## 2. 엔티티 상세

### User (사용자)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 식별자 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 로그인 ID |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 저장 |
| nickname | VARCHAR(50) | NOT NULL | 표시용 이름 |
| created_at | DATETIME | NOT NULL | 가입 시각 |

### Category (카테고리)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 카테고리 식별자 |
| user_id | BIGINT | FK → users.id, NOT NULL | 소유 사용자 |
| name | VARCHAR(50) | NOT NULL | 카테고리 이름(식비, 급여 등) |
| type | ENUM('INCOME','EXPENSE') | NOT NULL | 수입/지출 구분 |
| created_at | DATETIME | NOT NULL | 생성 시각 |

> 회원가입 시 지출(식비/교통/주거/문화)·수입(급여/용돈) 카테고리를 자동 시드한다.

### Transaction (거래내역)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 거래 식별자 |
| user_id | BIGINT | FK → users.id, NOT NULL | 기록한 사용자 |
| category_id | BIGINT | FK → categories.id, NOT NULL | 분류 카테고리 |
| type | ENUM('INCOME','EXPENSE') | NOT NULL | 수입/지출 |
| amount | BIGINT | NOT NULL, > 0 | 금액(원 단위, long) |
| description | VARCHAR(255) | NULL 허용 | 메모/설명 |
| transaction_date | DATE | NOT NULL | 거래 발생일 |
| created_at | DATETIME | NOT NULL | 등록 시각 |
| updated_at | DATETIME | NOT NULL | 수정 시각 |

### (도전) Budget (예산)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 예산 식별자 |
| user_id | BIGINT | FK → users.id, NOT NULL | 소유 사용자 |
| category_id | BIGINT | FK → categories.id, NULL 허용 | NULL이면 전체 예산 |
| year_month | VARCHAR(7) | NOT NULL | 대상 월("2026-07") |
| limit_amount | BIGINT | NOT NULL, > 0 | 한도 금액(원) |

## 3. 관계

모두 1:N 관계이며, FK는 N쪽 테이블(자식)에 위치한다.

| 관계 | 의미 | 방향 |
|------|------|------|
| User 1:N Category | 한 사용자가 여러 카테고리를 가진다 | User → Category |
| User 1:N Transaction | 한 사용자가 여러 거래를 가진다 | User → Transaction |
| Category 1:N Transaction | 한 카테고리에 여러 거래가 속한다 | Category → Transaction |
| User 1:N Budget (도전) | 한 사용자가 여러 예산을 가진다 | User → Budget |

**Transaction이 user_id와 category_id를 모두 갖는 이유**
- user_id 없으면 → "본인 데이터만" 인가(F-06)를 구현할 수 없다.
- category_id 없으면 → "카테고리별 지출 통계"(F-05)를 구할 수 없다.

## 4. ERD (텍스트 박스)

```
        ┌─────────────────┐
        │      USER       │
        │─────────────────│
        │ PK id           │
        │    email (UQ)   │
        │    password     │
        │    nickname     │
        │    created_at   │
        └───────┬─────────┘
       1        │        1                 1
   ┌────────────┼──────────────┐           └───────────┐
   │ N          │ N            │ N                      │ N (도전)
┌──▼───────────────┐   ┌───────▼────────────────┐  ┌───▼────────────────┐
│    CATEGORY      │   │     TRANSACTION        │  │      BUDGET        │
│──────────────────│   │────────────────────────│  │────────────────────│
│ PK id            │1  │ PK id                  │  │ PK id              │
│ FK user_id       │──<│ FK user_id             │  │ FK user_id         │
│    name          │ N │ FK category_id ────────┼─<│ FK category_id(NUL)│
│    type          │   │    type                │  │    year_month      │
│    created_at    │   │    amount              │  │    limit_amount    │
└──────────────────┘   │    description         │  └────────────────────┘
                       │    transaction_date    │
                       │    created_at          │
                       │    updated_at          │
                       └────────────────────────┘
```

## 5. 설계 결정 사항 (근거 포함)

| 고민 | 결정 | 근거 |
|------|------|------|
| 금액 타입 | long(BIGINT), 원 단위 정수 | 원화는 소수점이 없다. double은 부동소수 오차 위험, 이 규모에 BigDecimal은 과함 |
| 날짜 타입 | transactionDate는 DATE, created_at/updated_at은 DATETIME | 거래일은 날짜만 중요, 생성/수정 이력은 시각까지 필요 |
| type 저장 방식 | ENUM('INCOME','EXPENSE'), JPA에서 EnumType.STRING | 값 2개로 고정, ORDINAL은 순서 변경 시 데이터 훼손 위험 |
| 삭제 정책 | hard delete | 개인 가계부 특성상 삭제 이력 추적 요구 없음 |
| 카테고리 삭제 시 거래 처리 | 해당 카테고리를 참조하는 거래가 있으면 삭제를 막는다 (추후 "미분류" 이동으로 확장 가능) | 데이터 무결성 우선, 구현 단순성 |

## 6. 테이블 DDL (MySQL 8 기준)

```sql
-- 사용자
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 카테고리 (User 1:N Category)
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    type        ENUM('INCOME','EXPENSE') NOT NULL,
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 거래내역 (User 1:N Transaction, Category 1:N Transaction)
CREATE TABLE transactions (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    category_id       BIGINT       NOT NULL,
    type              ENUM('INCOME','EXPENSE') NOT NULL,
    amount            BIGINT       NOT NULL,
    description       VARCHAR(255) NULL,
    transaction_date  DATE         NOT NULL,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories(id),
    KEY idx_tx_user_date (user_id, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (도전) 예산
CREATE TABLE budgets (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    category_id   BIGINT      NULL,
    year_month    VARCHAR(7)  NOT NULL,
    limit_amount  BIGINT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 체크리스트

- [x] 요구사항에서 엔티티 4개(User/Category/Transaction/Budget) 도출
- [x] 각 엔티티의 컬럼·타입·제약을 표로 정리 (필드명이 SPEC과 일치)
- [x] 1:N 관계 4개 정의, FK 위치(N쪽) 확인
- [x] Transaction이 user_id와 category_id를 모두 가지는 이유 설명
- [x] ERD(텍스트 박스) 작성, PK/FK 표시
- [x] users/categories/transactions CREATE TABLE 작성
- [x] 금액 타입·날짜 타입·ENUM·삭제 정책 결정 및 근거 기록

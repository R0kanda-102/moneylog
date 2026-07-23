# 머니로그(MoneyLog) API 명세서 · 화면 흐름

- 작성자: 김태영
- 작성일: 2026-07-23
- 관련 문서: [docs/requirements.md](./requirements.md), [docs/erd.md](./erd.md)

## 1. 공통 규약

### 인증 헤더

로그인 이후 모든 요청에 아래 헤더를 포함한다.

```
Authorization: Bearer {accessToken}
```

- 헤더 없음/토큰 만료 → `401 UNAUTHORIZED`
- 유효 토큰이지만 남의 리소스 접근 → `403 FORBIDDEN`

### 성공 응답 봉투

```json
{ "success": true, "message": "...에 성공했습니다.", "data": { } }
```

목록 조회는 `meta.pagination`을 추가한다.

```json
{
  "success": true,
  "message": "거래내역 목록을 조회했습니다.",
  "data": { "transactions": [ ] },
  "meta": {
    "pagination": {
      "page": 0, "size": 20, "totalItems": 42,
      "totalPages": 3, "hasNext": true, "hasPrev": false
    }
  }
}
```

### 에러 응답 봉투

```json
{ "success": false, "code": "TRANSACTION_NOT_FOUND", "message": "거래내역을 찾을 수 없습니다.", "data": null }
```

### 표준 에러 코드

| HTTP 상태 | code | 상황 |
|---|---|---|
| 400 | VALIDATION_ERROR | 입력 검증 실패(금액 ≤ 0, 날짜 누락 등) |
| 401 | INVALID_CREDENTIALS | 로그인 시 이메일/비밀번호 불일치 |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |
| 403 | FORBIDDEN | 본인 데이터가 아님 |
| 409 | DUPLICATE_EMAIL | 이미 가입된 이메일 |
| 404 | CATEGORY_NOT_FOUND | 존재하지 않는 카테고리 |
| 404 | TRANSACTION_NOT_FOUND | 존재하지 않는 거래 |

## 2. API 명세

### 인증 (Auth)

| 메서드 | 경로 | 설명 | 요청 바디 | 인증 |
|---|---|---|---|:---:|
| POST | /api/auth/signup | 회원가입 | {email, password, nickname} | ❌ |
| POST | /api/auth/login | 로그인(JWT 발급) | {email, password} | ❌ |

### 카테고리 (Category)

| 메서드 | 경로 | 설명 | 요청 바디 | 인증 |
|---|---|---|---|:---:|
| GET | /api/categories | 내 카테고리 목록 | — | ✅ |
| POST | /api/categories | 카테고리 추가 | {name, type} | ✅ |
| PUT | /api/categories/{id} | 카테고리 수정 | {name, type} | ✅ |
| DELETE | /api/categories/{id} | 카테고리 삭제 | — | ✅ |

### 거래내역 (Transaction)

| 메서드 | 경로 | 설명 | 요청 바디 | 인증 |
|---|---|---|---|:---:|
| GET | /api/transactions?yearMonth=&type=&categoryId=&page=&size= | 목록(필터+페이징) | — | ✅ |
| POST | /api/transactions | 거래 등록 | {type, amount, categoryId, description, transactionDate} | ✅ |
| GET | /api/transactions/{id} | 거래 상세 | — | ✅ |
| PUT | /api/transactions/{id} | 거래 수정 | {type, amount, categoryId, description, transactionDate} | ✅ |
| DELETE | /api/transactions/{id} | 거래 삭제 | — | ✅ |

### 통계 (Statistics)

| 메서드 | 경로 | 설명 | 인증 |
|---|---|---|:---:|
| GET | /api/statistics/monthly?yearMonth= | 월별 통계(총수입/총지출/잔액/카테고리별) | ✅ |

### (도전) Budget

| 메서드 | 경로 | 설명 | 인증 |
|---|---|---|:---:|
| GET | /api/budgets?yearMonth= | 월 예산 조회 | ✅ |
| POST | /api/budgets | 예산 설정 | ✅ |

## 3. 요청/응답 예시

### 로그인 — POST /api/auth/login

요청:
```json
{ "email": "hong@moneylog.com", "password": "pass1234!" }
```

응답 (200):
```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": { "accessToken": "eyJhbGciOi..." }
}
```

### 거래 등록 — POST /api/transactions

요청:
```json
{
  "type": "EXPENSE",
  "amount": 12000,
  "categoryId": 3,
  "description": "점심 - 김치찌개",
  "transactionDate": "2026-07-08"
}
```

응답 (201):
```json
{
  "success": true,
  "message": "거래내역이 등록되었습니다.",
  "data": {
    "id": 42, "type": "EXPENSE", "amount": 12000,
    "categoryId": 3, "categoryName": "식비",
    "description": "점심 - 김치찌개",
    "transactionDate": "2026-07-08",
    "createdAt": "2026-07-08T12:31:05"
  }
}
```

### 거래 목록 — GET /api/transactions?yearMonth=2026-07&page=0&size=20

응답 (200):
```json
{
  "success": true,
  "message": "거래내역 목록을 조회했습니다.",
  "data": {
    "transactions": [
      { "id": 42, "type": "EXPENSE", "amount": 12000, "categoryId": 3, "categoryName": "식비", "description": "점심 - 김치찌개", "transactionDate": "2026-07-08" }
    ]
  },
  "meta": {
    "pagination": { "page": 0, "size": 20, "totalItems": 42, "totalPages": 3, "hasNext": true, "hasPrev": false }
  }
}
```

### 월별 통계 — GET /api/statistics/monthly?yearMonth=2026-07

응답 (200):
```json
{
  "success": true,
  "message": "월별 통계를 조회했습니다.",
  "data": {
    "income": 2500000,
    "expense": 830000,
    "balance": 1670000,
    "byCategory": [
      { "categoryName": "식비", "total": 420000 },
      { "categoryName": "교통", "total": 180000 }
    ]
  }
}
```

## 4. 화면 흐름

```
login.html ──(로그인 성공, 토큰 저장)──▶ transactions.html
                                            │
                          ┌─────────────────┴─────────────────┐
                          ▼                                     ▼
                   거래 등록/목록                          statistics.html
```

### 화면-API 매핑

| 화면 | 사용자 행동 | 호출 API |
|---|---|---|
| ① 로그인 | 회원가입 | POST /api/auth/signup |
| ① 로그인 | 로그인 → 토큰 저장 | POST /api/auth/login |
| ② 거래 목록 | 이번 달 목록 로드 | GET /api/transactions?yearMonth=...&page=0&size=20 |
| ② 거래 목록 | 필터용 카테고리 드롭다운 | GET /api/categories |
| ② 거래 목록 | 항목 삭제 | DELETE /api/transactions/{id} |
| ③ 거래 등록 | 카테고리 선택지 로드 | GET /api/categories |
| ③ 거래 등록 | 저장(신규) | POST /api/transactions |
| ③ 거래 등록 | 저장(수정 시) | PUT /api/transactions/{id} |
| ④ 월별 통계 | 이번 달 집계 로드 | GET /api/statistics/monthly?yearMonth=... |

## 5. API 개발 우선순위

요구사항 정의서의 Must(F-01~F-10) 순서를 따른다.

| 순위 | 묶음 | API | 이유 |
|---|---|---|---|
| 1 | 인증 | signup, login | 토큰이 있어야 나머지 API 테스트 가능 |
| 2 | 카테고리 | GET/POST/PUT/DELETE /categories | 거래가 카테고리를 참조하므로 먼저 |
| 3 | 거래 CRUD | GET/POST/PUT/DELETE /transactions | 앱의 핵심 데이터 |
| 4 | 목록 필터/페이징 | GET /transactions?... 고도화 | CRUD가 된 뒤 조회 조건 확장 |
| 5 | 통계 | GET /statistics/monthly | 거래 데이터가 쌓여야 집계 의미 있음 |
| 6 | 프론트 연동 | 화면 2종(+통계) | 위 API가 준비된 뒤 화면 연결 |
| 7 | (도전) budgets 등 | 도전 API | 기본이 끝난 뒤 가점 |

## 체크리스트

- [x] SPEC의 전체 엔드포인트를 인증/카테고리/거래/통계 4영역 표로 정리
- [x] 각 API의 인증 필요 여부(Bearer 토큰) 표시
- [x] 로그인·거래 등록·거래 목록·월별 통계 요청/응답 JSON을 공통 봉투로 작성
- [x] 목록 응답에 meta.pagination 포함
- [x] 에러 응답을 표준 봉투 + 표준 에러 코드로 통일
- [x] 화면 흐름도 + 화면-API 매핑 표 작성
- [x] API 개발 우선순위를 일차 일정과 맞춰 정리

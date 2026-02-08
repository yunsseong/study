# API 설계 (API Design)

## 좋은 API의 조건

```
1. 직관적 (URL만 보고 기능 파악)
2. 일관적 (네이밍, 응답 형식 통일)
3. 안전한 (인증/인가, Rate Limiting)
4. 확장 가능한 (버저닝, 페이지네이션)
5. 문서화 (Swagger/OpenAPI)
```

---

## 인증 (Authentication) vs 인가 (Authorization)

```
인증 (Authentication): "너 누구야?" → 신원 확인
인가 (Authorization):  "너 이거 할 수 있어?" → 권한 확인

1. 로그인 (인증) → JWT 토큰 발급
2. API 요청 시 토큰 첨부 → 서버가 확인 (인증)
3. 해당 리소스에 접근 권한 확인 (인가)
```

### 인증 방식 비교

| 방식 | 원리 | 적합 |
|------|------|------|
| **API Key** | 요청에 키 포함 | 서버 간 통신, 외부 API |
| **JWT** | 토큰에 정보 포함 | 모바일, SPA, MSA |
| **OAuth 2.0** | 제3자 인증 위임 | 소셜 로그인, 외부 서비스 연동 |
| **Session** | 서버에 상태 저장 | 전통적 웹 (SSR) |

### OAuth 2.0 흐름 (Authorization Code Grant)

```
1. 사용자 → "구글로 로그인" 클릭
2. 우리 서버 → 구글 인증 페이지로 리다이렉트
3. 사용자 → 구글에서 로그인 + 권한 동의
4. 구글 → 우리 서버로 Authorization Code 전달
5. 우리 서버 → 구글에 Code + Client Secret으로 Access Token 요청
6. 구글 → Access Token 발급
7. 우리 서버 → Access Token으로 구글 API 호출 (사용자 정보 조회)
8. 우리 서버 → 자체 JWT 발급하여 사용자에게 전달
```

---

## Rate Limiting

### 왜 필요한가?

```
- DDoS 공격 방어
- 서버 과부하 방지
- 공정한 리소스 분배
- API 남용 방지
```

### Rate Limiting 알고리즘

#### 1. 고정 윈도우 (Fixed Window)

```
1분에 100회 제한.

00:00~01:00 → 100회까지 허용
01:00~02:00 → 다시 100회

문제: 00:59에 100회 + 01:00에 100회 = 경계에서 200회 통과
```

#### 2. 슬라이딩 윈도우 (Sliding Window)

```
현재 시점 기준 직전 1분간 100회 제한.

현재 01:30이면 → 00:30~01:30 사이 요청 수 카운팅
→ 경계 문제 해결
```

#### 3. 토큰 버킷 (Token Bucket)

```
버킷에 토큰이 일정 속도로 충전.
요청마다 토큰 1개 소모. 토큰 없으면 거부.

버킷 크기: 10 (최대 버스트)
충전 속도: 1개/초

장점: 버스트 트래픽 허용하면서 평균 속도 제한
사용: AWS API Gateway, Nginx
```

### Redis로 Rate Limiting 구현

```python
# 슬라이딩 윈도우 방식
import redis
import time

r = redis.Redis()

def is_rate_limited(user_id: str, limit: int = 100, window: int = 60):
    key = f"rate:{user_id}"
    now = time.time()

    pipe = r.pipeline()
    pipe.zremrangebyscore(key, 0, now - window)  # 윈도우 밖 삭제
    pipe.zadd(key, {str(now): now})               # 현재 요청 추가
    pipe.zcard(key)                                # 요청 수 카운트
    pipe.expire(key, window)                       # TTL 설정
    results = pipe.execute()

    request_count = results[2]
    return request_count > limit  # True면 제한됨
```

### Rate Limiting 응답

```
HTTP/1.1 429 Too Many Requests
Retry-After: 30
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704067200
```

---

## 페이지네이션 (Pagination)

### 1. Offset 기반

```
GET /posts?page=1&size=20
GET /posts?page=2&size=20

SQL: SELECT * FROM posts ORDER BY id DESC LIMIT 20 OFFSET 20;

장점: 구현 단순, 특정 페이지 이동 가능
단점: OFFSET이 크면 느림 (앞의 행을 다 스캔)
```

### 2. 커서 기반 (No Offset) — 권장

```
GET /posts?cursor=12345&size=20
→ id < 12345인 게시글 20개

SQL: SELECT * FROM posts WHERE id < 12345 ORDER BY id DESC LIMIT 20;

장점: 일정한 성능 (인덱스로 바로 접근)
단점: 특정 페이지 이동 불가 (다음/이전만 가능)

응답:
{
  "data": [...],
  "nextCursor": "12325",
  "hasNext": true
}
```

### 3. 키셋 기반 (Keyset Pagination)

```
커서 기반의 확장. 정렬 기준이 여러 개일 때.

GET /posts?after_created=2026-01-01&after_id=12345&size=20

SQL: SELECT * FROM posts
     WHERE (created_at, id) < ('2026-01-01', 12345)
     ORDER BY created_at DESC, id DESC
     LIMIT 20;
```

```
선택 가이드:
├── 관리자 페이지 (페이지 이동 필요) → Offset 기반
├── 무한 스크롤 (SNS 피드) → 커서 기반
└── 대용량 + 정렬 기준 다양 → 키셋 기반
```

---

## API 버저닝

```
API가 변경될 때 기존 클라이언트가 깨지지 않도록.

방법 1: URL 경로 (가장 일반적)
  /api/v1/users
  /api/v2/users

방법 2: 헤더
  Accept: application/vnd.api.v2+json

방법 3: 쿼리 파라미터
  /api/users?version=2
```

---

## API 에러 응답 설계

```json
// 일관된 에러 응답 형식
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "이메일 형식이 올바르지 않습니다",
    "details": [
      {
        "field": "email",
        "message": "유효한 이메일 주소를 입력해주세요"
      }
    ]
  }
}
```

```
HTTP 상태 코드 활용:
400 Bad Request      → 잘못된 요청 (유효성 검증 실패)
401 Unauthorized     → 인증 필요
403 Forbidden        → 권한 없음
404 Not Found        → 리소스 없음
409 Conflict         → 충돌 (중복 등록 등)
429 Too Many Requests → Rate Limit 초과
500 Internal Server Error → 서버 오류
```

---

## Idempotency (멱등성)

```
같은 요청을 여러 번 보내도 결과가 동일.

멱등: GET, PUT, DELETE
비멱등: POST (여러 번 보내면 여러 개 생성)

POST를 멱등하게 만드는 방법:
  Idempotency Key 사용

  POST /orders
  Idempotency-Key: "abc-123-def"

  서버: "abc-123-def" 키로 이미 처리됨? → 같은 응답 반환
        처리 안 됨? → 처리 후 키와 결과 저장
```

---

## 면접 예상 질문

1. **인증과 인가의 차이는?**
   - 인증: 누구인지 확인 / 인가: 권한 확인

2. **Rate Limiting을 왜 하나요? 어떻게 구현하나요?**
   - 서버 보호, 남용 방지 / 토큰 버킷, 슬라이딩 윈도우 + Redis

3. **Offset 페이지네이션과 커서 페이지네이션의 차이는?**
   - Offset: 페이지 이동 가능, 대용량 느림 / 커서: 일정 성능, 다음/이전만

4. **API 멱등성이란?**
   - 같은 요청 반복 시 결과 동일, Idempotency Key로 POST도 멱등하게

5. **OAuth 2.0 흐름을 설명해주세요**
   - 인증 코드 요청 → 코드로 토큰 교환 → 토큰으로 리소스 접근

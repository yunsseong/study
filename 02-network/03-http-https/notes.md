# HTTP & HTTPS

## HTTP (HyperText Transfer Protocol)

### 특징
- **비연결 (Connectionless)**: 요청-응답 후 연결 종료 (HTTP/1.0)
- **무상태 (Stateless)**: 이전 요청의 상태를 기억하지 않음
- **텍스트 기반**: 사람이 읽을 수 있는 형태
- **클라이언트-서버 모델**: 요청(Request) → 응답(Response)

---

### HTTP 메서드

| 메서드 | 역할 | 멱등성 | 안전성 | 요청 Body |
|--------|------|--------|--------|----------|
| **GET** | 리소스 조회 | O | O | X |
| **POST** | 리소스 생성 | X | X | O |
| **PUT** | 리소스 전체 수정 | O | X | O |
| **PATCH** | 리소스 부분 수정 | X | X | O |
| **DELETE** | 리소스 삭제 | O | X | X |
| HEAD | 헤더만 조회 | O | O | X |
| OPTIONS | 지원 메서드 확인 | O | O | X |

**멱등성 (Idempotency)**: 같은 요청을 여러 번 보내도 결과가 동일
- GET: 10번 조회해도 같은 결과 → 멱등
- POST: 10번 요청하면 10개 생성 → 비멱등
- PUT: 10번 수정해도 최종 상태 동일 → 멱등
- DELETE: 이미 삭제된 걸 또 삭제해도 상태 동일 → 멱등

**안전성 (Safety)**: 서버의 상태를 변경하지 않음
- GET, HEAD, OPTIONS만 안전

---

### HTTP 상태 코드

| 범위 | 의미 | 대표 코드 |
|------|------|----------|
| **1xx** | 정보 | 100 Continue |
| **2xx** | 성공 | 200, 201, 204 |
| **3xx** | 리다이렉션 | 301, 302, 304 |
| **4xx** | 클라이언트 오류 | 400, 401, 403, 404, 409 |
| **5xx** | 서버 오류 | 500, 502, 503 |

#### 자주 쓰는 상태 코드

```
200 OK                    - 성공
201 Created               - 생성 성공 (POST)
204 No Content            - 성공, 반환할 본문 없음 (DELETE)

301 Moved Permanently     - 영구 이동 (URL 변경)
302 Found                 - 임시 이동
304 Not Modified          - 캐시 사용 (변경 없음)

400 Bad Request           - 잘못된 요청 (유효성 검증 실패)
401 Unauthorized          - 인증 필요 (로그인 안 됨)
403 Forbidden             - 권한 없음 (인가 실패)
404 Not Found             - 리소스 없음
409 Conflict              - 충돌 (중복 데이터)
429 Too Many Requests     - 요청 과다 (Rate Limit)

500 Internal Server Error - 서버 내부 오류
502 Bad Gateway           - 게이트웨이/프록시 오류
503 Service Unavailable   - 서비스 이용 불가 (과부하/점검)
```

**401 vs 403 차이** (면접 빈출)
- 401: "너 누구야?" → 인증(Authentication) 실패
- 403: "너인 건 아는데, 권한이 없어" → 인가(Authorization) 실패

---

### HTTP 요청/응답 구조

#### 요청 (Request)

```http
GET /users/1 HTTP/1.1
Host: api.example.com
Accept: application/json
Authorization: Bearer eyJhbGciOi...
User-Agent: Mozilla/5.0

(빈 줄)
(Body - GET은 보통 없음)
```

#### 응답 (Response)

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 85
Cache-Control: max-age=3600

{
  "id": 1,
  "name": "John",
  "email": "john@test.com"
}
```

### 주요 헤더

| 헤더 | 용도 | 예시 |
|------|------|------|
| Content-Type | 본문 타입 | application/json |
| Authorization | 인증 토큰 | Bearer {token} |
| Cache-Control | 캐시 정책 | max-age=3600, no-cache |
| Accept | 원하는 응답 형식 | application/json |
| Cookie | 쿠키 전송 | sessionId=abc123 |
| Set-Cookie | 쿠키 설정 (응답) | sessionId=abc123; HttpOnly |
| CORS 관련 | 교차 출처 허용 | Access-Control-Allow-Origin: * |

---

## HTTP 버전 비교

| 버전 | 특징 |
|------|------|
| HTTP/1.0 | 요청마다 새 연결 (비효율) |
| HTTP/1.1 | **Keep-Alive** (연결 재사용), 파이프라이닝 |
| HTTP/2 | **멀티플렉싱** (한 연결로 여러 요청 동시), 헤더 압축, 서버 푸시 |
| HTTP/3 | **QUIC** (UDP 기반), 더 빠른 연결 수립 |

### HTTP/1.1 vs HTTP/2

```
HTTP/1.1: 순차 요청 (Head-of-Line Blocking)
요청1 → 응답1 → 요청2 → 응답2 → 요청3 → 응답3

HTTP/2: 멀티플렉싱 (동시 처리)
요청1 ──→
요청2 ──→  하나의 연결로 동시에
요청3 ──→
     ←── 응답2
     ←── 응답1
     ←── 응답3
```

---

## HTTPS (HTTP + TLS/SSL)

### 왜 HTTPS가 필요한가?

HTTP는 **평문 전송** → 중간에서 도청/변조 가능

| 위협 | 설명 | HTTPS 해결 |
|------|------|-----------|
| 도청 (Eavesdropping) | 패킷 캡처로 데이터 읽기 | 암호화 |
| 변조 (Tampering) | 전송 중 데이터 수정 | 무결성 검증 |
| 위장 (Impersonation) | 가짜 서버로 속이기 | 인증서 검증 |

### TLS Handshake (간략화)

```
Client                          Server
  |                               |
  |--- Client Hello -----------→  |  ① 지원하는 암호화 방식 목록 전송
  |                               |
  |←-- Server Hello + 인증서 ---  |  ② 선택된 암호화 방식 + SSL 인증서
  |                               |
  |    (인증서 검증: CA 확인)      |  ③ 클라이언트가 인증서 유효성 검증
  |                               |
  |--- 대칭키 교환 (암호화) ---→  |  ④ 공개키로 대칭키(세션키) 암호화 전송
  |                               |
  |←-- Finished -----------------  |  ⑤ 양쪽 대칭키 확보, 암호화 통신 시작
  |                               |
  |===== 암호화된 HTTP 통신 =====|
```

### 핵심 원리

1. **비대칭키 (공개키/개인키)**: 처음에 대칭키를 안전하게 교환하는 용도
2. **대칭키 (세션키)**: 실제 데이터 암호화 (비대칭키보다 빠름)
3. **인증서 (Certificate)**: 서버의 신원 보증 (CA가 발급)

```
비대칭키: 느리지만 안전 → 키 교환에 사용
대칭키: 빠름 → 실제 데이터 암호화에 사용
∴ 둘을 조합하여 "안전하고 빠른" 통신 달성
```

### 인증서 체인

```
Root CA (최상위 인증기관)
  └── Intermediate CA (중간 인증기관)
        └── Server Certificate (서버 인증서)
```

- 브라우저에 Root CA 목록이 내장되어 있음
- 인증서 체인을 따라 올라가며 신뢰성 검증

---

## 캐시 (HTTP Caching)

### 캐시 제어 헤더

| 헤더 | 동작 |
|------|------|
| Cache-Control: max-age=3600 | 3600초간 캐시 사용 |
| Cache-Control: no-cache | 항상 서버에 재검증 요청 |
| Cache-Control: no-store | 절대 캐시하지 않음 |
| ETag: "abc123" | 리소스 버전 식별자 |
| If-None-Match: "abc123" | ETag 비교 요청 |

### 캐시 검증 흐름

```
첫 번째 요청:
Client → GET /image.png → Server
Client ← 200 OK + ETag: "v1" + 이미지 데이터

두 번째 요청:
Client → GET /image.png + If-None-Match: "v1" → Server
Server: ETag 비교 → 변경 없음
Client ← 304 Not Modified (본문 없음, 캐시 사용)
```

---

## 면접 예상 질문

1. **HTTP와 HTTPS의 차이는?**
   - HTTP: 평문 / HTTPS: TLS로 암호화, 인증서로 신원 보증

2. **TLS handshake 과정을 설명해주세요**
   - Client Hello → Server Hello + 인증서 → 대칭키 교환 → 암호화 통신

3. **대칭키와 비대칭키의 차이는?**
   - 대칭키: 같은 키로 암/복호화 (빠름) / 비대칭키: 공개키+개인키 (안전)

4. **HTTP 메서드의 멱등성이란?**
   - 같은 요청을 여러 번 보내도 결과가 동일한 성질

5. **401과 403의 차이는?**
   - 401: 인증 실패 / 403: 인가 실패

6. **HTTP/1.1과 HTTP/2의 차이는?**
   - 멀티플렉싱, 헤더 압축, 서버 푸시

7. **PUT과 PATCH의 차이는?**
   - PUT: 전체 교체 / PATCH: 부분 수정

# 쿠키, 세션, JWT

## 왜 필요한가?

HTTP는 **무상태(Stateless)** 프로토콜 → 서버는 이전 요청을 기억하지 못함.
로그인 상태를 유지하려면 별도의 메커니즘이 필요.

---

## 쿠키 (Cookie)

### 개념
- **브라우저(클라이언트)에 저장**되는 작은 데이터 (key=value)
- 서버가 `Set-Cookie` 헤더로 전달, 이후 요청마다 자동으로 `Cookie` 헤더에 포함

### 동작 흐름

```
1. 클라이언트 → POST /login (id, password)
2. 서버 → 응답 + Set-Cookie: userId=1
3. 이후 모든 요청에 Cookie: userId=1 자동 포함
```

### 쿠키 속성

| 속성 | 설명 |
|------|------|
| **HttpOnly** | JavaScript에서 접근 불가 (XSS 방어) |
| **Secure** | HTTPS에서만 전송 |
| **SameSite** | CSRF 방어 (Strict, Lax, None) |
| Domain | 쿠키가 전송되는 도메인 |
| Path | 쿠키가 전송되는 경로 |
| Max-Age / Expires | 만료 시간 (없으면 세션 쿠키 = 브라우저 닫으면 삭제) |

```http
Set-Cookie: sessionId=abc123; HttpOnly; Secure; SameSite=Lax; Max-Age=3600
```

### 쿠키의 한계
- 용량 제한: 약 4KB
- 매 요청마다 전송 → 트래픽 부담
- 클라이언트에서 조작 가능 → 민감 정보 저장 금지

---

## 세션 (Session)

### 개념
- 사용자 상태를 **서버에 저장**하는 방식
- 클라이언트는 **세션 ID만** 쿠키로 보유

### 동작 흐름

```
1. 클라이언트 → POST /login (id, password)
2. 서버: 세션 생성 (세션 저장소에 사용자 정보 저장)
   세션 저장소: { "abc123": { userId: 1, name: "John", role: "admin" } }
3. 서버 → 응답 + Set-Cookie: sessionId=abc123
4. 이후 요청: Cookie: sessionId=abc123
5. 서버: 세션 저장소에서 abc123으로 사용자 정보 조회
```

### 세션 저장소

| 저장소 | 특징 |
|--------|------|
| 서버 메모리 | 빠름, 서버 재시작 시 소멸, 단일 서버만 가능 |
| 파일 | 서버 재시작 후에도 유지 |
| DB (MySQL 등) | 영속적, 느림 |
| **Redis** | 빠름 + 영속적 + 다중 서버 공유 (권장) |

### 세션의 장단점

```
장점:
- 서버에서 관리 → 보안 강함 (클라이언트는 ID만 가짐)
- 강제 로그아웃 가능 (세션 삭제하면 됨)
- 민감 정보가 네트워크를 타지 않음

단점:
- 서버 메모리/저장소 부담
- 다중 서버 환경에서 세션 공유 문제 (Sticky Session 또는 Redis 필요)
- 확장성 제한 (서버가 상태를 가짐)
```

### 다중 서버 세션 공유 문제

```
서버 A: 세션 abc123 저장
서버 B: 세션 abc123 모름 → 인증 실패!

해결법:
1. Sticky Session: 로드밸런서가 같은 서버로 라우팅
2. 세션 클러스터링: 서버 간 세션 복제
3. 중앙 저장소: Redis에 세션 저장 (가장 일반적)
```

---

## JWT (JSON Web Token)

### 개념
- 사용자 정보를 **토큰 자체에 포함**시키는 방식
- 서버가 상태를 저장하지 않음 → **무상태(Stateless)**
- 서명(Signature)으로 위변조 검증

### JWT 구조

```
xxxxx.yyyyy.zzzzz
Header.Payload.Signature

Header (알고리즘, 타입):
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload (사용자 정보 - Claims):
{
  "sub": "1",           ← 사용자 ID
  "name": "John",
  "role": "admin",
  "iat": 1705000000,    ← 발급 시간
  "exp": 1705003600     ← 만료 시간
}

Signature (위변조 방지):
HMACSHA256(
  base64(header) + "." + base64(payload),
  secret_key
)
```

> Payload는 **Base64 인코딩** (암호화가 아님!) → 누구나 디코딩 가능
> 따라서 **비밀번호 같은 민감 정보를 넣으면 안 됨**

### 동작 흐름

```
1. 클라이언트 → POST /login (id, password)
2. 서버: 인증 성공 → JWT 생성 → 응답
3. 클라이언트: 토큰을 저장 (localStorage 또는 쿠키)
4. 이후 요청: Authorization: Bearer eyJhbGciOi...
5. 서버: 토큰 검증 (서명 확인 + 만료 시간 확인) → 사용자 정보 추출
```

### Access Token + Refresh Token

Access Token만 사용하면?
- 만료 시간이 길면 → 탈취 시 위험
- 만료 시간이 짧으면 → 자주 로그인해야 함

```
Access Token:  유효기간 짧음 (15분~1시간)  - API 인증용
Refresh Token: 유효기간 김 (7일~30일)      - Access Token 재발급용

흐름:
1. 로그인 → Access Token + Refresh Token 발급
2. API 요청 → Access Token 사용
3. Access Token 만료 → Refresh Token으로 재발급 요청
4. 새 Access Token 발급
5. Refresh Token도 만료 → 재로그인 필요
```

### JWT의 장단점

```
장점:
- 서버가 상태를 저장하지 않음 → 확장성 우수 (수평 확장 용이)
- DB 조회 없이 토큰 자체로 인증 가능 → 빠름
- 다중 서버 환경에서 별도 세션 공유 불필요
- MSA, 모바일 앱에 적합

단점:
- 토큰 크기가 큼 (매 요청마다 전송)
- 발급 후 강제 만료 불가 (로그아웃 처리 어려움)
- Payload 정보가 노출됨 (인코딩 ≠ 암호화)
- 토큰 탈취 시 만료까지 사용 가능
```

### JWT 로그아웃 처리

JWT는 서버에서 무효화할 수 없으므로 추가 전략 필요:

| 방법 | 설명 |
|------|------|
| 블랙리스트 | Redis에 로그아웃된 토큰 저장, 요청 시 확인 |
| 짧은 만료 시간 | Access Token을 짧게 설정 (15분) |
| 토큰 버전 관리 | DB에 토큰 버전 저장, 불일치 시 거부 |

---

## 세션 vs JWT 비교 (면접 핵심)

| 비교 | 세션 | JWT |
|------|------|-----|
| 저장 위치 | 서버 | 클라이언트 (토큰 자체) |
| 서버 상태 | Stateful | **Stateless** |
| 확장성 | 세션 공유 필요 | 우수 (서버 확장 용이) |
| 보안 | 서버 관리 (강제 만료 가능) | 탈취 시 만료까지 유효 |
| 성능 | 매 요청마다 저장소 조회 | DB 조회 불필요 |
| 용량 | 세션 ID만 전송 (작음) | 토큰 전체 전송 (큼) |
| 로그아웃 | 세션 삭제로 즉시 가능 | 즉시 무효화 어려움 |
| 적합한 환경 | 단일/소수 서버, 웹 | MSA, 모바일, 대규모 |

### 어떤 걸 선택해야 하나?

```
세션 선택:
- 보안이 최우선 (금융, 의료)
- 강제 로그아웃이 필요
- 서버 규모가 크지 않음

JWT 선택:
- 서버 확장이 중요 (MSA, 대규모)
- 모바일 앱 지원
- 서버 간 인증 정보 공유 필요
- Stateless가 중요

실무에서는:
- JWT (Access + Refresh) + Redis 블랙리스트 조합이 가장 일반적
```

---

## 보안 관련

### XSS (Cross-Site Scripting) 방어

```
위험: localStorage에 JWT 저장 시 XSS로 탈취 가능

방어:
- JWT를 HttpOnly 쿠키에 저장 (JavaScript 접근 불가)
- 입력값 이스케이프 처리
```

### CSRF (Cross-Site Request Forgery) 방어

```
위험: 쿠키가 자동 전송되므로 악성 사이트에서 요청 위조 가능

방어:
- SameSite=Strict 쿠키 속성
- CSRF 토큰 사용
- Authorization 헤더 방식 (쿠키 대신)
```

### 토큰 저장 위치 비교

| 저장 위치 | XSS 취약 | CSRF 취약 | 권장 |
|----------|---------|----------|------|
| localStorage | O (위험) | X | X |
| sessionStorage | O (위험) | X | X |
| **HttpOnly Cookie** | X (안전) | O (방어 가능) | **O** |

---

## 면접 예상 질문

1. **쿠키와 세션의 차이는?**
   - 쿠키: 클라이언트 저장 / 세션: 서버 저장, 쿠키로 세션 ID만 전달

2. **세션과 JWT의 차이는?**
   - Stateful vs Stateless, 확장성, 보안, 로그아웃 처리 차이

3. **JWT의 구조를 설명해주세요**
   - Header.Payload.Signature, Base64 인코딩, 서명으로 위변조 검증

4. **JWT의 단점과 보완 방법은?**
   - 강제 만료 불가 → 블랙리스트, 짧은 만료 + Refresh Token

5. **Access Token과 Refresh Token은 왜 분리하나요?**
   - 보안(짧은 만료)과 편의성(재로그인 최소화) 균형

6. **JWT를 어디에 저장해야 하나요?**
   - HttpOnly Cookie 권장 (XSS 방어)

7. **다중 서버에서 세션을 어떻게 관리하나요?**
   - Redis 중앙 세션 저장소

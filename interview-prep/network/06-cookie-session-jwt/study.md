# 6. Cookie / Session / JWT

---

## HTTP는 상태를 기억하지 못한다

HTTP는 **Stateless(무상태)** 프로토콜이다.
요청 하나하나가 독립적이라, 서버는 이전 요청을 기억하지 못한다.

```
[클라이언트] → "로그인: id=kim, pw=1234"      → [서버] "로그인 성공!"
[클라이언트] → "내 프로필 보여줘"              → [서버] "너 누구야?"
```

서버 입장에서 두 번째 요청은 **완전히 새로운 사람**이다.
로그인했다는 사실을 서버가 기억하지 못한다.

이 문제를 해결하기 위해 **Cookie, Session, JWT**가 등장했다.
세 가지 모두 "이 사람이 누구인지"를 유지하기 위한 방법이다.

---

## Cookie란?

**Cookie**: 서버가 브라우저에 저장하라고 보내는 **작은 데이터(key=value)**

### 동작 원리

```
[1단계: 서버가 Cookie를 설정]

[클라이언트] → POST /login (id=kim, pw=1234) → [서버]
[클라이언트] ← HTTP 응답                      ← [서버]
                Set-Cookie: user=kim

[2단계: 브라우저가 Cookie를 자동 전송]

[클라이언트] → GET /profile                   → [서버]
                Cookie: user=kim
[클라이언트] ← "kim님의 프로필"                ← [서버]
```

핵심 흐름:
1. 서버가 응답 헤더에 `Set-Cookie: user=kim`을 담아 보낸다
2. 브라우저가 이 Cookie를 저장한다
3. 이후 **같은 도메인**으로 요청할 때마다 `Cookie: user=kim`을 자동으로 보낸다
4. 서버가 Cookie를 읽어서 누구인지 판단한다

### Cookie 주요 속성

| 속성 | 설명 | 예시 |
|------|------|------|
| **Domain** | Cookie가 전송되는 도메인 | `Domain=myapp.com` |
| **Path** | Cookie가 전송되는 경로 | `Path=/api` |
| **Expires/Max-Age** | 만료 시간 | `Max-Age=3600` (1시간) |
| **HttpOnly** | JavaScript에서 접근 불가 | `HttpOnly` (XSS 방어) |
| **Secure** | HTTPS에서만 전송 | `Secure` |
| **SameSite** | 다른 사이트에서 전송 제한 | `SameSite=Strict` (CSRF 방어) |

보안에 중요한 3가지:

```
HttpOnly  → document.cookie로 접근 불가 → XSS 공격 방어
Secure    → HTTPS에서만 전송           → 도청 방어
SameSite  → 다른 사이트 요청 시 전송 차단 → CSRF 공격 방어
```

### SameSite 옵션 정리

```
SameSite=Strict  → 다른 사이트에서 온 요청에 Cookie 안 보냄 (가장 안전)
SameSite=Lax     → GET 요청에는 보냄, POST에는 안 보냄 (기본값)
SameSite=None    → 항상 보냄 (Secure 필수, 서드파티 Cookie)
```

---

## Session이란?

**Session**: 사용자 상태를 **서버에 저장**하고, 클라이언트에는 **Session ID만** 전달하는 방식

### 동작 원리

```
[1단계: 로그인 → 서버가 Session 생성]

[클라이언트] → POST /login (id=kim, pw=1234) → [서버]
                                                  Session 저장소에 저장:
                                                  { "abc123": { user: "kim", role: "USER" } }
[클라이언트] ← Set-Cookie: JSESSIONID=abc123  ← [서버]

[2단계: 이후 요청 → Session ID로 사용자 식별]

[클라이언트] → GET /profile                   → [서버]
                Cookie: JSESSIONID=abc123         Session 저장소에서 조회:
                                                  "abc123" → { user: "kim", role: "USER" }
[클라이언트] ← "kim님의 프로필"                ← [서버]
```

핵심 흐름:
1. 서버가 로그인 성공 시 **Session 객체**를 생성하고 저장한다
2. Session ID를 Cookie에 담아 클라이언트에 보낸다
3. 클라이언트는 매 요청마다 Session ID(Cookie)를 보낸다
4. 서버가 Session ID로 저장소를 조회해서 사용자 정보를 확인한다

**중요**: Cookie에는 Session ID만 있고, 실제 데이터는 서버에 있다.

---

## Cookie vs Session 비교

| 비교 항목 | Cookie | Session |
|-----------|--------|---------|
| **저장 위치** | 클라이언트(브라우저) | 서버 (메모리/DB/Redis) |
| **저장 데이터** | 실제 값 (user=kim) | Session ID만 전달 |
| **보안** | 데이터 노출 위험 | 서버에 저장되어 안전 |
| **용량** | 4KB 제한 | 서버 메모리만큼 |
| **서버 부하** | 없음 | 사용자 수만큼 메모리 사용 |
| **만료** | Expires/Max-Age | 서버에서 만료 관리 |

```
Cookie만 사용:  데이터가 브라우저에 있으므로 조작/노출 위험
Session 사용:   데이터가 서버에 있으므로 안전, but 서버 부하 증가
```

**면접 포인트**: "Session도 결국 Cookie를 사용합니다. Session ID를 Cookie로 주고받기 때문입니다. 차이는 실제 데이터가 어디에 저장되느냐입니다."

---

## Session의 한계 - 서버 확장 문제

서버가 한 대면 문제없지만, **여러 대로 확장(Scale-out)**하면 문제가 생긴다.

```
[문제 상황]

[클라이언트] → POST /login → [서버 A] → Session 저장: { abc123: kim }
                                                       ↑ 서버 A만 알고 있음!
[클라이언트] → GET /profile → [서버 B] → Session에 abc123 없음!
                (로드밸런서가 서버 B로 보냄)           → "누구세요?" 401 Unauthorized
```

서버 A에서 만든 Session을 서버 B는 모른다!

### 해결 방법 3가지

**1. Sticky Session (고정 세션)**

```
[로드밸런서] → 같은 클라이언트는 항상 같은 서버로 보냄
                클라이언트 kim → 항상 서버 A

장점: 구현 간단
단점: 서버 A가 죽으면 kim의 Session도 사라짐
      특정 서버에 부하 집중 가능
```

**2. Session Clustering (세션 복제)**

```
[서버 A] ⇄ Session 동기화 ⇄ [서버 B]
[서버 A] ⇄ Session 동기화 ⇄ [서버 C]

모든 서버가 모든 Session을 가지고 있음

장점: 어떤 서버로 가도 OK
단점: 서버가 많아질수록 동기화 비용 폭증
      메모리 낭비 (모든 서버가 모든 Session 저장)
```

**3. 외부 Session 저장소 (Redis)**

```
[서버 A] ──→ [Redis] ←── [서버 B]
[서버 C] ──→ [Redis]

모든 서버가 Redis에서 Session 조회

장점: 서버가 몇 대든 상관없음, Session이 중앙화됨
단점: Redis 장애 시 전체 Session 불능
      네트워크 홉 추가 (약간의 지연)
```

**실무에서는 3번(Redis)이 가장 많이 쓰인다.** Spring Boot에서는 Spring Session + Redis로 쉽게 구현 가능하다.

---

## JWT (JSON Web Token)란?

**JWT**: 사용자 정보를 **토큰 자체에 담아** 클라이언트에게 발급하는 방식

Session과 반대 발상이다:
- Session: "서버가 기억한다" → 서버에 저장
- JWT: "토큰이 증명한다" → 서버에 저장하지 않음 (Stateless)

### JWT 구조

JWT는 점(.)으로 구분된 3개의 Base64 인코딩 문자열이다.

```
xxxxx.yyyyy.zzzzz
  |      |      |
Header.Payload.Signature
```

각 부분의 역할:

```
[Header] - 토큰 타입과 서명 알고리즘
{
  "alg": "HS256",
  "typ": "JWT"
}

[Payload] - 실제 데이터 (Claims)
{
  "sub": "1234",        // 사용자 ID
  "name": "kim",        // 사용자 이름
  "role": "USER",       // 권한
  "iat": 1700000000,    // 발급 시간
  "exp": 1700003600     // 만료 시간 (1시간 후)
}

[Signature] - 위변조 방지 서명
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

**핵심**: Payload는 암호화가 아니라 **인코딩**이다. 누구나 디코딩해서 읽을 수 있다.
Signature 덕분에 **위변조 여부**만 검증할 수 있다. 비밀번호 같은 민감 정보는 Payload에 넣으면 안 된다.

### JWT 동작 원리

```
[1단계: 로그인 → JWT 발급]

[클라이언트] → POST /login (id=kim, pw=1234) → [서버]
                                                  비밀번호 검증 OK
                                                  JWT 생성 (Header.Payload.Signature)
[클라이언트] ← { "token": "eyJhb..." }        ← [서버]

[2단계: 이후 요청 → JWT 전송]

[클라이언트] → GET /profile                   → [서버]
                Authorization: Bearer eyJhb...     JWT 검증:
                                                   1. Signature 확인 (위변조?)
                                                   2. exp 확인 (만료?)
                                                   3. Payload에서 사용자 정보 추출
[클라이언트] ← "kim님의 프로필"                ← [서버]
```

핵심 흐름:
1. 서버가 로그인 성공 시 JWT를 생성해서 클라이언트에 발급한다
2. 클라이언트가 JWT를 저장한다 (보통 LocalStorage 또는 Cookie)
3. 매 요청마다 `Authorization: Bearer <토큰>` 헤더에 JWT를 담아 보낸다
4. 서버는 JWT의 Signature를 검증하고 Payload에서 사용자 정보를 꺼낸다

**서버는 Session처럼 저장소를 조회하지 않는다. 토큰 자체에 정보가 있다.**

---

## JWT 장단점

| 장점 | 단점 |
|------|------|
| **Stateless** - 서버에 저장 안 함 | **토큰 크기가 큼** - Cookie/Session ID보다 큼 |
| **서버 확장 쉬움** - 어느 서버든 검증 가능 | **탈취 시 무효화 어려움** - 만료까지 유효 |
| **서버 부하 없음** - DB/Redis 조회 불필요 | **Payload 노출** - 민감 정보 넣으면 안 됨 |
| **마이크로서비스에 적합** - 서비스 간 인증 | **토큰 갱신 복잡** - Refresh Token 필요 |

**면접 포인트**: "JWT는 서버 확장에 유리하지만, 한 번 발급하면 서버에서 강제로 무효화하기 어렵습니다. 이것이 Session 대비 가장 큰 단점입니다."

---

## Access Token + Refresh Token 전략

JWT의 "탈취되면 만료까지 유효" 문제를 해결하기 위한 전략이다.

```
Access Token:  짧은 수명 (15분~1시간)  → 실제 API 요청에 사용
Refresh Token: 긴 수명 (7일~30일)      → Access Token 재발급에 사용
```

### 동작 흐름

```
[1단계: 로그인 → 토큰 2개 발급]

[클라이언트] → POST /login          → [서버]
[클라이언트] ← {                    ← [서버]
                 accessToken: "eyJ...",    (15분)
                 refreshToken: "dGh..."    (7일)
               }

[2단계: API 요청 → Access Token 사용]

[클라이언트] → GET /profile                      → [서버]
                Authorization: Bearer eyJ...         Access Token 검증 OK
[클라이언트] ← "kim님의 프로필"                   ← [서버]

[3단계: Access Token 만료 → Refresh Token으로 재발급]

[클라이언트] → GET /profile                      → [서버]
                Authorization: Bearer eyJ...         Access Token 만료!
[클라이언트] ← 401 Unauthorized                   ← [서버]

[클라이언트] → POST /auth/refresh                 → [서버]
                { refreshToken: "dGh..." }           Refresh Token 검증 OK
[클라이언트] ← { accessToken: "NEW_eyJ..." }      ← [서버] 새 Access Token 발급

[4단계: 새 Access Token으로 재요청]

[클라이언트] → GET /profile                      → [서버]
                Authorization: Bearer NEW_eyJ...     검증 OK
[클라이언트] ← "kim님의 프로필"                   ← [서버]
```

**왜 이렇게 나누나?**
- Access Token이 탈취되어도 15분 후 만료된다
- Refresh Token은 서버 DB에 저장하여 강제 무효화(로그아웃, 비밀번호 변경)가 가능하다
- 사용자는 7일간 재로그인 없이 사용할 수 있다

---

## JWT를 어디에 저장할 것인가?

이 질문은 면접에서 자주 나온다. 정답은 없고 **trade-off**를 아는 것이 중요하다.

### LocalStorage vs Cookie 비교

| 비교 | LocalStorage | Cookie (HttpOnly) |
|------|-------------|-------------------|
| **XSS 취약** | O (JS에서 접근 가능) | X (HttpOnly면 JS 접근 불가) |
| **CSRF 취약** | X (자동 전송 안 됨) | O (Cookie는 자동 전송) |
| **구현 난이도** | 쉬움 (JS로 관리) | CSRF 방어 추가 필요 |
| **서버 전송 방식** | Authorization 헤더에 수동 첨부 | 브라우저가 자동 전송 |

```
XSS (Cross-Site Scripting):
  → 악성 스크립트가 페이지에서 실행되어 토큰을 훔쳐감
  → LocalStorage는 document.cookie처럼 JS로 접근 가능 → 취약
  → HttpOnly Cookie는 JS에서 접근 불가 → 안전

CSRF (Cross-Site Request Forgery):
  → 다른 사이트에서 사용자 모르게 요청을 보냄
  → Cookie는 자동으로 포함되므로 → 취약
  → LocalStorage는 명시적으로 헤더에 넣어야 하므로 → 안전
```

### 실무 권장 패턴

```
[가장 안전한 조합]

Access Token  → HttpOnly, Secure, SameSite Cookie에 저장
Refresh Token → HttpOnly, Secure, SameSite Cookie에 저장
CSRF Token    → 별도로 발급하여 CSRF 공격 방어

장점: XSS에 안전 (HttpOnly), CSRF도 방어 (SameSite + CSRF Token)
단점: 구현이 복잡해짐

[간단한 방법 - 소규모 프로젝트]

Access Token  → LocalStorage에 저장
Refresh Token → HttpOnly Cookie에 저장

장점: 구현 간단, Access Token은 어차피 수명 짧음
단점: XSS에 Access Token 노출 가능 (수명이 짧아 위험도 낮음)
```

---

## Spring Boot에서 Session 관리

Spring Boot는 `HttpSession`으로 Session을 쉽게 관리할 수 있다.

### 기본 Session 사용

```java
@PostMapping("/login")
public String login(@RequestBody LoginRequest request, HttpSession session) {
    // 1. 사용자 검증
    User user = userService.authenticate(request.getId(), request.getPassword());

    // 2. Session에 사용자 정보 저장
    session.setAttribute("user", user);

    return "로그인 성공";
    // 자동으로 Set-Cookie: JSESSIONID=abc123 응답
}

@GetMapping("/profile")
public User getProfile(HttpSession session) {
    // 3. Session에서 사용자 정보 조회
    User user = (User) session.getAttribute("user");

    if (user == null) {
        throw new UnauthorizedException("로그인이 필요합니다");
    }

    return user;
}

@PostMapping("/logout")
public String logout(HttpSession session) {
    // 4. Session 무효화 (삭제)
    session.invalidate();
    return "로그아웃 성공";
}
```

### Spring Session + Redis (서버 확장 시)

```yaml
# application.yml
spring:
  session:
    store-type: redis
  redis:
    host: localhost
    port: 6379
```

```java
// build.gradle
dependencies {
    implementation 'org.springframework.session:spring-session-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
}
```

이것만 설정하면 Session이 자동으로 Redis에 저장된다. 코드 변경 없이 서버 확장 문제 해결.

---

## Spring Security + JWT 인증 흐름

실무에서 JWT를 사용할 때는 Spring Security와 함께 사용한다.

### 전체 흐름

```
[로그인 요청]
POST /auth/login { id: "kim", pw: "1234" }
       │
       ↓
[AuthController]
  → AuthService.login(id, pw)
  → UserRepository에서 사용자 조회
  → 비밀번호 검증 (BCryptPasswordEncoder)
  → JwtTokenProvider.createToken(userId, role)
  → Access Token + Refresh Token 반환
       │
       ↓
[이후 API 요청]
GET /api/users
Authorization: Bearer eyJhb...
       │
       ↓
[JwtAuthenticationFilter] (Spring Security Filter Chain)
  → Authorization 헤더에서 토큰 추출
  → JwtTokenProvider.validateToken(token) → Signature, 만료 검증
  → 토큰에서 userId, role 추출
  → SecurityContextHolder에 인증 정보 저장
       │
       ↓
[Controller]
  → @AuthenticationPrincipal로 사용자 정보 접근
  → 비즈니스 로직 수행
```

### 핵심 구성 요소

```
JwtTokenProvider      : JWT 생성, 검증, 파싱 담당
JwtAuthenticationFilter: 매 요청마다 JWT를 검사하는 필터
SecurityConfig        : Spring Security 설정 (어떤 URL에 인증 필요한지)
```

### JwtTokenProvider 핵심 코드

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000L;  // 15분

    public String createToken(Long userId, String role) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(
            Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token).getBody().getSubject()
        );
    }
}
```

### JwtAuthenticationFilter 핵심 코드

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 검증 및 인증 정보 설정
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);
            // SecurityContext에 인증 정보 저장
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

---

## OAuth 2.0 기본 개념

OAuth 2.0은 **제3자 서비스를 통해 인증/인가**하는 프로토콜이다.
"구글 로그인", "카카오 로그인" 같은 소셜 로그인이 OAuth 2.0 기반이다.

### 핵심 용어

```
Resource Owner    : 사용자 (나)
Client            : 우리 서비스 (myapp.com)
Authorization Server : 인증 서버 (Google, Kakao)
Resource Server   : 리소스 서버 (Google API, Kakao API)
```

### 간단한 흐름 (Authorization Code Grant)

```
[1] 사용자가 "구글로 로그인" 클릭

[2] 구글 로그인 페이지로 이동 (redirect)
    → 사용자가 구글에 로그인 + 권한 동의

[3] 구글이 Authorization Code를 우리 서버에 전달
    → myapp.com/callback?code=abc123

[4] 우리 서버가 Authorization Code로 Access Token 요청
    → 구글 서버에 code + client_secret 전송

[5] 구글이 Access Token 발급
    → 우리 서버가 이 토큰으로 구글 API 호출 가능

[6] 구글 API에서 사용자 정보 (이메일, 이름) 가져옴
    → 우리 서비스에 회원가입/로그인 처리
    → 우리 서비스의 JWT 발급
```

**면접 포인트**: "OAuth의 Access Token은 구글 API를 호출하기 위한 것이고, 우리 서비스의 JWT와는 별개입니다. 소셜 로그인 후 우리 서비스 자체의 JWT를 발급하는 것이 일반적입니다."

---

## Session vs JWT 최종 비교

| 비교 항목 | Session | JWT |
|-----------|---------|-----|
| **상태 관리** | Stateful (서버 저장) | Stateless (토큰에 저장) |
| **저장 위치** | 서버 (메모리/Redis) | 클라이언트 (Cookie/LocalStorage) |
| **서버 확장** | 어려움 (공유 저장소 필요) | 쉬움 (서버 저장 불필요) |
| **강제 로그아웃** | 쉬움 (Session 삭제) | 어려움 (만료까지 유효) |
| **보안** | 서버에 데이터 → 안전 | 토큰 탈취 시 위험 |
| **서버 부하** | Session 조회 비용 | 검증만 (경량) |
| **적합한 환경** | 단일 서버, 웹 앱 | MSA, 모바일, API 서버 |

```
Session 적합:  전통적인 웹 앱, 강제 로그아웃 필수, 단일 서버
JWT 적합:     마이크로서비스, 모바일 앱 API, 서버 확장 필요
실무:         대부분 JWT + Redis (Refresh Token 서버 저장) 조합
```

---

## 면접 핵심 정리

**Q: Cookie와 Session의 차이는 무엇인가요?**
> Cookie는 데이터를 클라이언트(브라우저)에 저장하고, Session은 서버에 저장합니다.
> Session은 Session ID를 Cookie에 담아 전달하므로 Cookie를 사용합니다.
> 실제 데이터가 어디에 저장되느냐가 핵심 차이입니다.
> Session이 더 안전하지만 서버 메모리를 사용하고, Cookie는 용량 제한(4KB)이 있습니다.

**Q: JWT란 무엇이고, Session과 비교하면?**
> JWT는 사용자 정보를 토큰 자체에 담아 발급하는 인증 방식입니다.
> Session은 서버에 상태를 저장하는 Stateful 방식이고,
> JWT는 서버에 저장하지 않는 Stateless 방식입니다.
> JWT는 서버 확장에 유리하지만 강제 로그아웃이 어렵고,
> Session은 강제 로그아웃이 쉽지만 서버 확장 시 Redis 같은 공유 저장소가 필요합니다.

**Q: Access Token과 Refresh Token은 왜 분리하나요?**
> Access Token의 수명을 짧게(15분) 설정하여 탈취 피해를 줄이고,
> Refresh Token으로 재발급하여 사용자 편의성을 유지합니다.
> Refresh Token은 서버 DB에 저장하여 강제 무효화(로그아웃)가 가능합니다.

**Q: JWT를 LocalStorage에 저장하면 어떤 보안 문제가 있나요?**
> XSS 공격에 취약합니다. 악성 스크립트가 localStorage.getItem()으로 토큰을 탈취할 수 있습니다.
> HttpOnly Cookie에 저장하면 JavaScript 접근이 불가하여 XSS에 안전하지만,
> CSRF 공격에 대비해야 합니다. SameSite 속성과 CSRF Token으로 방어할 수 있습니다.

**Q: 서버가 여러 대일 때 Session 관리는 어떻게 하나요?**
> 세 가지 방법이 있습니다. Sticky Session은 같은 클라이언트를 같은 서버로 보내지만
> 서버 장애 시 Session이 유실됩니다. Session Clustering은 서버 간 Session을 복제하지만
> 서버가 많아질수록 동기화 비용이 증가합니다.
> 실무에서는 Redis 같은 외부 저장소에 Session을 저장하는 방법을 가장 많이 사용합니다.
> Spring Boot에서는 Spring Session + Redis로 코드 변경 없이 구현할 수 있습니다.

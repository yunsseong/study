# 5. HTTP/HTTPS

---

## HTTP란?

**HTTP (HyperText Transfer Protocol)**: 웹에서 클라이언트와 서버가 데이터를 주고받는 규약(프로토콜)

```
[클라이언트(브라우저)]                    [서버(Spring Boot)]
       |                                       |
       |  --- HTTP 요청 (GET /api/users) --->   |
       |                                       |
       |  <--- HTTP 응답 (200 OK + JSON) ---   |
       |                                       |
```

웹의 모든 통신은 HTTP 위에서 동작한다.
브라우저가 서버에 "이것 주세요"라고 요청하고, 서버가 "여기 있습니다"라고 응답하는 구조다.

---

## HTTP의 특성

### 1. 비연결성 (Connectionless)

요청-응답이 끝나면 연결을 끊는다.

```
[클라이언트]                    [서버]
    | --- 요청1: GET /users ---> |
    | <--- 응답1: 200 OK ---     |
    | (연결 끊김)                 |
    |                            |
    | --- 요청2: GET /posts ---> |
    | <--- 응답2: 200 OK ---     |
    | (연결 끊김)                 |
```

- 장점: 서버 자원을 효율적으로 사용 (수천 명이 접속해도 동시에 연결 유지할 필요 없음)
- 단점: 매 요청마다 TCP 연결을 새로 맺어야 함 (오버헤드)
- 개선: HTTP/1.1의 **Keep-Alive**로 연결을 재사용

### 2. 무상태 (Stateless)

서버가 클라이언트의 이전 요청을 기억하지 않는다.

```
요청1: "로그인합니다 (ID: kim)"   → 서버: "로그인 성공"
요청2: "내 정보 보여줘"           → 서버: "누구세요?" (이전 요청을 모름)
```

- 장점: 서버 확장(Scale-Out)이 쉬움. 어떤 서버가 받아도 동일하게 처리 가능
- 단점: 매 요청마다 인증 정보를 보내야 함
- 해결: **쿠키, 세션, JWT** 등으로 상태를 유지

```
무상태의 장점 - 서버 확장:

요청1 → [서버 A] 처리 OK
요청2 → [서버 B] 처리 OK  ← 상태를 서버에 저장하지 않으므로 어떤 서버든 OK
요청3 → [서버 C] 처리 OK

상태가 있다면:
요청1 → [서버 A] 로그인 정보 저장
요청2 → [서버 B] "누구세요?" ← 서버 A에만 정보가 있어서 문제!
```

---

## HTTP 메서드

### 주요 메서드 정리

| 메서드 | 역할 | 멱등성 | 요청 Body | Spring 매핑 |
|--------|------|--------|-----------|------------|
| **GET** | 리소스 조회 | O | 없음 | `@GetMapping` |
| **POST** | 리소스 생성 | X | 있음 | `@PostMapping` |
| **PUT** | 리소스 전체 수정 | O | 있음 | `@PutMapping` |
| **PATCH** | 리소스 부분 수정 | O | 있음 | `@PatchMapping` |
| **DELETE** | 리소스 삭제 | O | 없음/있음 | `@DeleteMapping` |

### 멱등성 (Idempotent)이란?

같은 요청을 여러 번 보내도 결과가 동일한 것.

```
GET /users/1   → 1번 보내든 100번 보내든 같은 유저 정보 반환 (멱등)
DELETE /users/1 → 1번 삭제든 100번 요청이든 결과는 "삭제됨" (멱등)
POST /users    → 보낼 때마다 새 유저 생성 (멱등 아님!)
```

멱등성이 중요한 이유: 네트워크 장애로 요청이 중복 전송될 수 있다.
멱등한 메서드는 재시도해도 안전하다.

### GET vs POST 상세 비교

| 구분 | GET | POST |
|------|-----|------|
| **목적** | 데이터 조회 | 데이터 생성/처리 |
| **데이터 전달** | URL 쿼리 스트링 | 요청 Body |
| **URL 예시** | `/users?name=kim&age=25` | `/users` (Body에 데이터) |
| **캐시** | O (브라우저가 캐시) | X (캐시하지 않음) |
| **멱등성** | O | X |
| **북마크** | O (URL에 데이터 포함) | X |
| **데이터 길이** | URL 길이 제한 있음 | 제한 없음 |
| **보안** | URL에 노출 (브라우저 히스토리, 로그) | Body에 포함 (상대적으로 나음) |

```
GET 요청:
GET /api/users?name=kim&age=25 HTTP/1.1
Host: myapp.com

POST 요청:
POST /api/users HTTP/1.1
Host: myapp.com
Content-Type: application/json

{"name": "kim", "age": 25}
```

주의: POST도 HTTPS 없이는 Body가 평문이므로 "안전하다"고 할 수 없다.
진짜 보안은 HTTPS로 해결한다.

### PUT vs PATCH 차이

```
현재 데이터:
{
  "id": 1,
  "name": "kim",
  "age": 25,
  "email": "kim@test.com"
}

PUT /users/1  (전체 수정 - 모든 필드를 보내야 함)
{
  "name": "kim",
  "age": 26,
  "email": "kim@test.com"
}
→ 빠진 필드가 있으면 null로 덮어쓸 수 있음

PATCH /users/1  (부분 수정 - 바꿀 필드만)
{
  "age": 26
}
→ age만 변경, 나머지는 그대로 유지
```

실무에서는 PATCH를 더 자주 사용한다. 매번 모든 필드를 보내는 것은 비효율적이기 때문이다.

---

## HTTP 상태 코드

### 1xx: 정보 (Informational)

거의 쓸 일 없다. `100 Continue` 정도만 알면 된다.

### 2xx: 성공 (Success)

| 코드 | 의미 | 사용 예시 |
|------|------|----------|
| **200** | OK - 요청 성공 | GET 조회 성공 |
| **201** | Created - 생성 성공 | POST로 리소스 생성 |
| **204** | No Content - 성공, 응답 Body 없음 | DELETE 성공 |

```java
// Spring Boot 예시
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));  // 200
}

@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
    User user = userService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);  // 201
}

@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204
}
```

### 3xx: 리다이렉션 (Redirection)

| 코드 | 의미 | 차이점 |
|------|------|--------|
| **301** | Moved Permanently (영구 이동) | 브라우저가 새 URL을 캐시, 메서드 GET으로 변경 가능 |
| **302** | Found (임시 이동) | 캐시하지 않음, 메서드 GET으로 변경 가능 |
| **304** | Not Modified (변경 없음) | 캐시된 리소스를 그대로 사용 |

```
301: http://old-site.com → https://new-site.com  (영구 이전)
302: http://myapp.com/event → /event-2024         (임시 이벤트 페이지)
304: 브라우저 캐시에 있는 이미지/CSS가 아직 유효함   (서버가 "바뀐 거 없어" 응답)
```

### 4xx: 클라이언트 에러

| 코드 | 의미 | 언제 발생? |
|------|------|-----------|
| **400** | Bad Request | 잘못된 요청 (파라미터 오류, 유효성 검증 실패) |
| **401** | Unauthorized | 인증 안 됨 (로그인 필요) |
| **403** | Forbidden | 권한 없음 (로그인은 했지만 접근 권한 없음) |
| **404** | Not Found | 리소스 없음 (URL이 잘못되었거나 데이터 없음) |

```
401 vs 403 차이:

401 Unauthorized: "너 누구야? 로그인부터 해"
  → 토큰 없음, 토큰 만료

403 Forbidden: "너가 누군지는 알지만, 이건 못 봐"
  → 일반 사용자가 관리자 페이지 접근
```

### 5xx: 서버 에러

| 코드 | 의미 | 언제 발생? |
|------|------|-----------|
| **500** | Internal Server Error | 서버 내부 오류 (NullPointerException 등) |
| **502** | Bad Gateway | 게이트웨이/프록시가 백엔드 서버에서 잘못된 응답 받음 |
| **503** | Service Unavailable | 서버 과부하 또는 점검 중 |

```
502: [클라이언트] → [Nginx] → [Spring Boot (죽어있음)] → Nginx가 502 반환
503: [클라이언트] → [서버 (과부하/점검 중)] → 503 반환
```

---

## HTTP 헤더

### 주요 헤더

| 헤더 | 역할 | 예시 |
|------|------|------|
| **Content-Type** | 요청/응답 데이터 형식 | `application/json`, `text/html` |
| **Authorization** | 인증 정보 | `Bearer eyJhbGciOi...` |
| **Cache-Control** | 캐시 정책 | `no-cache`, `max-age=3600` |
| **Accept** | 클라이언트가 원하는 응답 형식 | `application/json` |
| **Cookie** | 쿠키 정보 | `JSESSIONID=abc123` |
| **Set-Cookie** | 서버가 쿠키 설정 | `JSESSIONID=abc123; Path=/` |
| **Host** | 요청 대상 서버 | `myapp.com` |

```
HTTP 요청 예시:

GET /api/users HTTP/1.1
Host: myapp.com
Accept: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Cache-Control: no-cache

HTTP 응답 예시:

HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: max-age=3600
Set-Cookie: JSESSIONID=abc123; Path=/; HttpOnly

[{"id": 1, "name": "kim"}, {"id": 2, "name": "lee"}]
```

### Content-Type 주요 값

```
application/json        → REST API에서 가장 많이 사용
application/x-www-form-urlencoded  → HTML 폼 기본 전송
multipart/form-data     → 파일 업로드
text/html               → HTML 문서
text/plain              → 일반 텍스트
```

---

## HTTP 버전 비교

### HTTP/1.0 vs HTTP/1.1 vs HTTP/2.0 vs HTTP/3.0

| 구분 | HTTP/1.0 | HTTP/1.1 | HTTP/2.0 | HTTP/3.0 |
|------|----------|----------|----------|----------|
| **연결** | 요청마다 새 연결 | Keep-Alive (연결 재사용) | Multiplexing | Multiplexing |
| **전송** | 순차 전송 | Pipelining (제한적) | 바이너리 프레임 | 바이너리 프레임 |
| **헤더** | 매번 전체 전송 | 매번 전체 전송 | HPACK 압축 | QPACK 압축 |
| **프로토콜** | TCP | TCP | TCP | **UDP (QUIC)** |
| **HOL Blocking** | 있음 | 있음 | TCP 레벨 있음 | 해결 |

### HTTP/1.0의 문제

```
[클라이언트]          [서버]
   | -- TCP 연결 -->    |
   | -- GET /a -->      |
   | <-- 응답 /a --     |
   | -- TCP 종료 -->    |  ← 연결 끊김
   |                    |
   | -- TCP 연결 -->    |  ← 다시 연결!
   | -- GET /b -->      |
   | <-- 응답 /b --     |
   | -- TCP 종료 -->    |  ← 또 끊김

→ 요청마다 TCP 3-way handshake + 4-way handshake = 느리다
```

### HTTP/1.1 Keep-Alive

하나의 TCP 연결로 여러 요청을 보낸다.

```
[클라이언트]          [서버]
   | -- TCP 연결 -->    |
   | -- GET /a -->      |
   | <-- 응답 /a --     |
   | -- GET /b -->      |  ← 연결을 끊지 않고 재사용!
   | <-- 응답 /b --     |
   | -- GET /c -->      |
   | <-- 응답 /c --     |
   | -- TCP 종료 -->    |

→ TCP 연결 1번으로 여러 요청 처리 (효율적)
```

### HTTP/1.1 Pipelining 문제 (HOL Blocking)

Pipelining: 응답을 기다리지 않고 여러 요청을 연속으로 보내는 것.
그러나 **HOL(Head-of-Line) Blocking** 문제가 있다.

```
Pipelining:
[클라이언트]          [서버]
   | -- GET /a -->      |
   | -- GET /b -->      |  ← 응답 안 기다리고 바로 전송
   | -- GET /c -->      |
   | <-- 응답 /a --     |  ← 반드시 요청 순서대로 응답해야 함
   | <-- 응답 /b --     |
   | <-- 응답 /c --     |

HOL Blocking 문제:
   | -- GET /a (큰 파일) -->  |
   | -- GET /b (작은 파일) --> |
   | <-- 응답 /a (느림...) --  |  ← /a가 오래 걸리면
   |         ...대기...        |  ← /b도 기다려야 함!
   | <-- 응답 /b --            |

→ 실무에서는 Pipelining을 거의 사용하지 않았다
→ 대신 브라우저가 도메인당 6~8개 TCP 연결을 동시에 열어서 해결
```

### HTTP/2.0 핵심 기능

#### 1. Multiplexing (다중화)

하나의 TCP 연결에서 여러 요청/응답을 **동시에** 주고받는다.
순서에 관계없이 완성된 것부터 응답한다.

```
HTTP/1.1:
연결1: [요청A] → [응답A]
연결2: [요청B] → [응답B]
연결3: [요청C] → [응답C]
→ 여러 TCP 연결 필요

HTTP/2.0:
하나의 연결:
  스트림1: [요청A] -----> [응답A]
  스트림2: [요청B] --> [응답B]        ← B가 먼저 완료되면 먼저 응답
  스트림3: [요청C] --------> [응답C]
→ 하나의 TCP 연결로 동시 처리
```

#### 2. Header Compression (HPACK)

HTTP 요청마다 반복되는 헤더를 압축한다.

```
HTTP/1.1 - 매 요청마다 같은 헤더를 그대로 보냄:
요청1: Host: myapp.com, Accept: application/json, Authorization: Bearer abc...
요청2: Host: myapp.com, Accept: application/json, Authorization: Bearer abc...
→ 중복!

HTTP/2.0 - 헤더 테이블로 중복 제거:
요청1: Host: myapp.com, Accept: application/json, Authorization: Bearer abc...
요청2: (인덱스 참조만 전송) → 대폭 줄어듬
→ 이전에 보낸 헤더와 같으면 인덱스만 전송
```

#### 3. Server Push

클라이언트가 요청하지 않은 리소스를 서버가 미리 보내준다.

```
클라이언트: GET /index.html

HTTP/1.1:
  서버 → index.html 응답
  클라이언트가 HTML 파싱 → style.css, app.js 필요하다는 걸 알게 됨
  클라이언트 → GET /style.css, GET /app.js 요청
  서버 → 응답

HTTP/2.0 Server Push:
  서버 → index.html 응답
  서버 → style.css도 같이 보냄 (PUSH)
  서버 → app.js도 같이 보냄 (PUSH)
  → 클라이언트가 추가 요청할 필요 없음!
```

### HTTP/3.0

TCP 대신 **QUIC (UDP 기반)** 프로토콜을 사용한다.

```
HTTP/2.0의 한계:
  TCP 연결에서 패킷 하나가 유실되면 → 전체 스트림이 대기 (TCP HOL Blocking)

HTTP/3.0 (QUIC):
  UDP 기반이므로 스트림이 독립적
  스트림1에서 패킷 유실 → 스트림2, 3은 영향 없이 계속 진행
  연결 설정도 더 빠름 (0-RTT 가능)
```

```
연결 설정 비교:

HTTP/1.1 + HTTPS:
  TCP 3-way handshake (1 RTT) + TLS handshake (2 RTT) = 3 RTT

HTTP/2.0 + HTTPS:
  TCP 3-way handshake (1 RTT) + TLS handshake (2 RTT) = 3 RTT (같음)

HTTP/3.0 (QUIC):
  QUIC handshake (1 RTT, TCP+TLS 통합) = 1 RTT
  재연결 시: 0-RTT 가능!
```

---

## HTTPS란?

**HTTPS = HTTP + TLS/SSL**

HTTP 통신을 **암호화**하여 중간에서 데이터를 도청/변조할 수 없게 한 것.

```
HTTP:
[클라이언트] --- "password=1234" (평문) ---> [서버]
                  ↑ 해커가 볼 수 있음!

HTTPS:
[클라이언트] --- "x7#kQ9$mP..." (암호화) ---> [서버]
                  ↑ 해커가 봐도 해독 불가
```

---

## 대칭키 vs 비대칭키 암호화

### 대칭키 (Symmetric Key)

암호화와 복호화에 **같은 키**를 사용한다.

```
[A] "안녕하세요" → 키로 암호화 → "x7#kQ9" → 같은 키로 복호화 → "안녕하세요" [B]

키: abc123 (A와 B 모두 같은 키 사용)
```

- 장점: **빠르다** (연산이 단순)
- 단점: 키를 어떻게 안전하게 전달하나? (키가 노출되면 끝)

### 비대칭키 (Asymmetric Key = 공개키 암호화)

**공개키(Public Key)**와 **개인키(Private Key)** 쌍을 사용한다.

```
서버가 키 쌍을 만든다:
  공개키 (Public Key): 누구에게나 공개
  개인키 (Private Key): 서버만 가지고 있음

공개키로 암호화 → 개인키로만 복호화 가능
개인키로 암호화 → 공개키로만 복호화 가능

[클라이언트]                           [서버]
    |  ← 공개키 전달 ---                 |
    |  "1234"를 공개키로 암호화           |
    |  --- "x7#kQ9" 전송 -->            |
    |                    개인키로 복호화 → "1234"
```

- 장점: 키 교환 문제 해결 (공개키는 노출되어도 안전)
- 단점: **느리다** (연산이 복잡, 대칭키 대비 100~1000배)

### HTTPS는 둘 다 쓴다!

```
1. 비대칭키로 안전하게 "대칭키"를 교환
2. 이후 통신은 대칭키로 암호화 (빠르게)

→ 비대칭키의 안전성 + 대칭키의 속도 = 최적 조합
```

---

## HTTPS 동작 원리 (TLS Handshake)

### 전체 과정

```
[클라이언트]                                    [서버]
    |                                             |
    |  ① Client Hello                             |
    |  (지원하는 TLS 버전, 암호 알고리즘 목록,     |
    |   클라이언트 랜덤값)                         |
    |  ---------------------------------------->  |
    |                                             |
    |  ② Server Hello                             |
    |  (선택한 TLS 버전, 암호 알고리즘,            |
    |   서버 랜덤값, SSL 인증서)                   |
    |  <----------------------------------------  |
    |                                             |
    |  ③ 인증서 검증                               |
    |  CA(인증기관)의 공개키로 인증서 서명 검증      |
    |  → 서버가 진짜인지 확인                       |
    |                                             |
    |  ④ 대칭키 생성 & 교환                        |
    |  클라이언트가 Pre-Master Secret 생성          |
    |  서버의 공개키로 암호화하여 전송               |
    |  ---------------------------------------->  |
    |                                             |
    |  서버가 개인키로 복호화                       |
    |  양쪽 모두: 클라이언트 랜덤 + 서버 랜덤       |
    |  + Pre-Master Secret → 대칭키(세션키) 생성    |
    |                                             |
    |  ⑤ Finished (핸드셰이크 완료)                |
    |  <--------- 암호화 통신 시작 --------->      |
    |                                             |
    |  ⑥ 이후 모든 통신은 대칭키로 암호화           |
    |  ---------------------------------------->  |
    |  <----------------------------------------  |
```

### SSL 인증서란?

서버의 신원을 보증하는 디지털 증명서. **CA(Certificate Authority, 인증기관)**가 발급한다.

```
인증서에 포함된 정보:
  - 서버 도메인 (myapp.com)
  - 서버의 공개키
  - 인증기관(CA) 정보
  - CA의 디지털 서명
  - 유효 기간

검증 과정:
  브라우저에 미리 설치된 CA의 공개키로
  → 인증서의 서명을 검증
  → "이 인증서는 진짜 CA가 발급한 것이다"
  → "이 서버는 진짜 myapp.com이다"
```

---

## Spring Boot에서 HTTPS 설정

### 1. 인증서 준비

```bash
# 개발용 자체 서명 인증서 생성 (keytool)
keytool -genkeypair -alias myapp \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365

# 운영환경에서는 Let's Encrypt 등 CA 인증서 사용
```

### 2. application.yml 설정

```yaml
server:
  port: 443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
    key-alias: myapp
```

### 3. HTTP → HTTPS 리다이렉트

```java
@Configuration
public class HttpsConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(80);
        connector.setSecure(false);
        connector.setRedirectPort(443);
        return connector;
    }
}
```

실무에서는 보통 Nginx나 AWS ALB 같은 로드 밸런서에서 SSL을 처리하고,
Spring Boot 서버에는 HTTP로 전달하는 **SSL Termination** 방식을 많이 사용한다.

```
[클라이언트] --- HTTPS ---> [Nginx / ALB] --- HTTP ---> [Spring Boot]
                            (SSL 처리)                  (내부 통신이므로 HTTP OK)
```

---

## 면접 핵심 정리

**Q: HTTP의 특성인 비연결성과 무상태를 설명해주세요.**
> HTTP는 비연결성과 무상태라는 두 가지 특성이 있습니다.
> 비연결성은 요청-응답이 끝나면 연결을 끊는 것으로, 서버 자원을 효율적으로 사용할 수 있습니다.
> HTTP/1.1부터 Keep-Alive로 연결을 재사용하여 단점을 보완합니다.
> 무상태는 서버가 이전 요청 정보를 기억하지 않는 것으로, 서버 확장이 쉬운 장점이 있습니다.
> 상태 유지가 필요한 경우 쿠키, 세션, JWT 등을 사용합니다.

**Q: HTTP와 HTTPS의 차이는 무엇인가요?**
> HTTPS는 HTTP에 TLS/SSL 암호화를 추가한 것입니다.
> HTTP는 데이터를 평문으로 전송하여 도청, 변조가 가능하지만
> HTTPS는 TLS Handshake로 대칭키를 안전하게 교환하고, 이후 통신을 암호화합니다.
> 비대칭키로 안전하게 대칭키를 교환하고, 대칭키로 빠르게 암호화하는 하이브리드 방식입니다.

**Q: HTTP/1.1과 HTTP/2.0의 차이를 설명해주세요.**
> HTTP/1.1은 Keep-Alive로 연결을 재사용하지만 하나의 연결에서 순차적으로 요청/응답합니다.
> HOL Blocking 문제가 있어 앞 요청이 느리면 뒷 요청도 지연됩니다.
> HTTP/2.0은 Multiplexing으로 하나의 연결에서 여러 요청/응답을 동시에 처리합니다.
> 또한 HPACK으로 헤더를 압축하고, Server Push로 필요한 리소스를 미리 전송할 수 있습니다.

**Q: GET과 POST의 차이를 설명해주세요.**
> GET은 리소스 조회용으로 데이터를 URL 쿼리 스트링에 담아 보냅니다.
> 캐시 가능하고, 멱등성이 있어 여러 번 요청해도 같은 결과입니다.
> POST는 리소스 생성/처리용으로 데이터를 Body에 담아 보냅니다.
> 캐시되지 않고, 멱등성이 없어 요청할 때마다 새 리소스가 생성될 수 있습니다.
> 보안 측면에서 GET은 URL에 데이터가 노출되므로 민감한 데이터는 POST를 사용하되,
> 진정한 보안은 HTTPS로 보장합니다.

**Q: HTTPS의 TLS Handshake 과정을 설명해주세요.**
> 1. Client Hello: 클라이언트가 지원하는 TLS 버전, 암호 알고리즘, 랜덤값을 서버에 전송합니다.
> 2. Server Hello: 서버가 사용할 알고리즘을 선택하고, 랜덤값과 SSL 인증서를 전송합니다.
> 3. 인증서 검증: 클라이언트가 CA의 공개키로 인증서의 서명을 검증하여 서버 신원을 확인합니다.
> 4. 대칭키 교환: 클라이언트가 Pre-Master Secret을 서버의 공개키로 암호화하여 전송합니다.
>    양쪽이 랜덤값들과 Pre-Master Secret으로 동일한 세션키(대칭키)를 생성합니다.
> 5. 이후 모든 통신은 이 대칭키로 암호화하여 빠르고 안전하게 통신합니다.

# 5. Spring Security

---

## Spring Security란?

**Spring Security**: Spring 기반 애플리케이션의 **인증(Authentication)**과 **인가(Authorization)**를 담당하는 보안 프레임워크.

```
Spring Security가 해결하는 문제:

[클라이언트 요청]
     │
     ▼
┌─────────────────────────────┐
│ Spring Security Filter Chain │  ← 요청을 가로채서 보안 처리
│                              │
│ 1. 이 사용자가 누구인가? (인증) │
│ 2. 이 사용자가 접근 가능한가? (인가) │
│ 3. CSRF, CORS 등 보안 정책    │
└─────────────────────────────┘
     │
     ▼ (통과 시)
[Controller → Service → Repository]
```

**왜 필요한가?**

```java
// Spring Security 없이 직접 구현하면?
@RestController
public class UserController {

    @GetMapping("/admin/users")
    public List<User> getUsers(HttpServletRequest request) {
        // 매 요청마다 수동으로 검증해야 함
        String token = request.getHeader("Authorization");
        if (token == null) {
            throw new UnauthorizedException("로그인 필요");
        }
        User user = tokenProvider.validateAndGetUser(token);
        if (!user.getRole().equals("ADMIN")) {
            throw new ForbiddenException("권한 없음");
        }
        return userService.findAll();
    }
}
// → 모든 컨트롤러에 반복 코드, 실수 가능성 높음

// Spring Security 사용 시
@RestController
public class UserController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<User> getUsers() {
        return userService.findAll();  // 보안 로직은 Security가 처리
    }
}
```

---

## Spring Security 아키텍처 (Filter Chain 구조)

### Servlet Filter와 Spring Security의 관계

```
[클라이언트 HTTP 요청]
         │
         ▼
┌─────────────────────────────────────────┐
│          Servlet Container (Tomcat)       │
│                                          │
│  [Filter 1] → [Filter 2] → ... → [DispatcherServlet]
│                    ↑                      │
│          DelegatingFilterProxy             │
│                    │                      │
│          FilterChainProxy                  │
│                    │                      │
│     ┌──────────────┼──────────────┐       │
│     ▼              ▼              ▼       │
│  [Security    [Security     [Security     │
│   Filter 1]   Filter 2]     Filter N]     │
│                                          │
│  ← 이것이 SecurityFilterChain →           │
└─────────────────────────────────────────┘
```

```
핵심 구조:

DelegatingFilterProxy
  └── FilterChainProxy (springSecurityFilterChain)
        └── SecurityFilterChain
              ├── SecurityContextPersistenceFilter
              ├── LogoutFilter
              ├── UsernamePasswordAuthenticationFilter
              ├── BasicAuthenticationFilter
              ├── BearerTokenAuthenticationFilter
              ├── AuthorizationFilter
              └── FilterSecurityInterceptor (deprecated → AuthorizationFilter)
```

> **핵심**: Spring Security는 **Servlet Filter 기반**으로 동작한다.
> 요청이 DispatcherServlet(Controller)에 도달하기 전에 보안 처리를 수행한다.

---

## SecurityFilterChain 주요 필터들

### 필터 실행 순서와 역할

```
HTTP 요청 → [SecurityFilterChain 진입]

① SecurityContextPersistenceFilter
   └── SecurityContext 로드/저장 (세션에서 인증 정보 복원)

② LogoutFilter
   └── 로그아웃 URL(/logout) 요청 처리

③ UsernamePasswordAuthenticationFilter
   └── Form Login 시 username/password 인증 처리
   └── POST /login 요청을 가로챔

④ BasicAuthenticationFilter
   └── HTTP Basic 인증 (Authorization: Basic base64...)

⑤ BearerTokenAuthenticationFilter (또는 Custom JWT Filter)
   └── JWT Bearer 토큰 인증

⑥ ExceptionTranslationFilter
   └── 인증/인가 예외를 HTTP 응답으로 변환 (401, 403)

⑦ AuthorizationFilter
   └── 최종 인가(권한) 검사

→ [DispatcherServlet] → [Controller]
```

### 주요 필터 상세

| 필터 | 역할 | 동작 시점 |
|------|------|----------|
| **SecurityContextPersistenceFilter** | SecurityContext를 세션에서 로드/저장 | 모든 요청 |
| **UsernamePasswordAuthenticationFilter** | Form Login 인증 처리 | POST /login |
| **BasicAuthenticationFilter** | HTTP Basic 인증 | Authorization: Basic 헤더 |
| **BearerTokenAuthenticationFilter** | OAuth2/JWT 토큰 인증 | Authorization: Bearer 헤더 |
| **ExceptionTranslationFilter** | 보안 예외를 HTTP 응답으로 변환 | 예외 발생 시 |
| **AuthorizationFilter** | URL별 접근 권한 검사 | 모든 요청 |

---

## 인증(Authentication) vs 인가(Authorization) 차이

```
인증 (Authentication)              인가 (Authorization)
─────────────────────              ─────────────────────
"너는 누구인가?"                    "너는 이것을 할 수 있는가?"

로그인 과정                         권한 검사 과정
ID/PW 확인, 토큰 검증               ROLE, AUTHORITY 확인

401 Unauthorized                   403 Forbidden
(인증 실패)                         (인가 실패)

먼저 수행                           인증 후 수행
```

| 구분 | 인증 (Authentication) | 인가 (Authorization) |
|------|----------------------|---------------------|
| **질문** | 이 사용자가 누구인가? | 이 사용자가 접근 가능한가? |
| **방법** | ID/PW, JWT, OAuth | Role, Authority, @PreAuthorize |
| **실패 시** | 401 Unauthorized | 403 Forbidden |
| **순서** | 1단계 (먼저) | 2단계 (인증 후) |
| **예시** | 로그인 | 관리자 페이지 접근 |

---

## Authentication 객체와 SecurityContext

### SecurityContext 구조

```
SecurityContextHolder (ThreadLocal)
  └── SecurityContext
        └── Authentication
              ├── Principal     → 사용자 정보 (UserDetails)
              ├── Credentials   → 인증 수단 (비밀번호, 토큰 등)
              ├── Authorities   → 권한 목록 (ROLE_USER, ROLE_ADMIN)
              └── isAuthenticated() → 인증 여부
```

```java
// Authentication 객체 구조
public interface Authentication extends Principal {
    Collection<? extends GrantedAuthority> getAuthorities();  // 권한 목록
    Object getCredentials();  // 비밀번호 등 인증 수단
    Object getDetails();      // 추가 정보 (IP, 세션 등)
    Object getPrincipal();    // 사용자 정보 (UserDetails)
    boolean isAuthenticated();
}
```

```java
// SecurityContext에서 인증 정보 가져오기
@GetMapping("/me")
public String getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    String username = auth.getName();                          // 사용자명
    Object principal = auth.getPrincipal();                    // UserDetails
    Collection<? extends GrantedAuthority> roles = auth.getAuthorities();  // 권한

    return username;
}

// 또는 @AuthenticationPrincipal 사용 (더 깔끔)
@GetMapping("/me")
public String getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    return userDetails.getUsername();
}
```

> **핵심**: SecurityContextHolder는 **ThreadLocal** 기반이므로,
> 같은 스레드 내 어디서든 `SecurityContextHolder.getContext().getAuthentication()`으로
> 현재 인증된 사용자 정보에 접근할 수 있다.

---

## AuthenticationManager → AuthenticationProvider → UserDetailsService 흐름

### 인증 처리 전체 흐름

```
[UsernamePasswordAuthenticationFilter]
         │
         │ UsernamePasswordAuthenticationToken(username, password)
         ▼
[AuthenticationManager] (인터페이스)
         │
         │ authenticate(Authentication)
         ▼
[ProviderManager] (구현체)
         │
         │ 등록된 AuthenticationProvider들을 순회
         ▼
[AuthenticationProvider] (인터페이스)
         │
         │ ① UserDetailsService.loadUserByUsername(username) 호출
         │ ② 반환된 UserDetails의 password와 입력 password 비교
         │ ③ PasswordEncoder.matches(rawPassword, encodedPassword)
         ▼
[UserDetailsService]
         │
         │ loadUserByUsername(username) → DB에서 사용자 조회
         ▼
[UserDetails] 반환
         │
         │ 인증 성공 시
         ▼
[Authentication 객체] (인증 완료, Authorities 포함)
         │
         ▼
[SecurityContextHolder에 저장]
```

```java
// 각 컴포넌트의 역할 요약
AuthenticationManager     // 인증 요청을 받는 진입점 (인터페이스)
  └── ProviderManager     // AuthenticationProvider 목록을 관리하는 구현체
        └── AuthenticationProvider    // 실제 인증 로직 수행
              ├── UserDetailsService  // DB에서 사용자 조회
              └── PasswordEncoder     // 비밀번호 검증
```

---

## UserDetails, UserDetailsService 구현

### UserDetails 인터페이스

```java
public interface UserDetails extends Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();  // 권한 목록
    String getPassword();          // 암호화된 비밀번호
    String getUsername();          // 사용자 식별자
    boolean isAccountNonExpired(); // 계정 만료 여부
    boolean isAccountNonLocked();  // 계정 잠금 여부
    boolean isCredentialsNonExpired();  // 인증 정보 만료 여부
    boolean isEnabled();           // 계정 활성화 여부
}
```

### 구현 예시

```java
// 1. UserDetails 구현체
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;  // DB Entity

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired()     { return true; }
    @Override
    public boolean isAccountNonLocked()      { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled()               { return true; }
}
```

```java
// 2. UserDetailsService 구현체
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new CustomUserDetails(user);
    }
}
```

---

## PasswordEncoder (BCrypt)

```java
// Bean 등록
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt 해시 알고리즘 사용
    }
}
```

```java
// 사용 예시
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 시 비밀번호 암호화
    public User register(SignupRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // encode("1234") → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

        User user = new User(request.getEmail(), encodedPassword, Role.USER);
        return userRepository.save(user);
    }

    // 로그인 시 비밀번호 검증 (Spring Security가 자동으로 호출)
    // passwordEncoder.matches("1234", encodedPassword) → true/false
}
```

```
BCrypt 특징:

1. 단방향 해시: 원본 복원 불가능
2. Salt 자동 생성: 같은 비밀번호도 매번 다른 해시값
3. 느린 해싱: 의도적으로 느려서 Brute Force 방어
4. 비용 인자(Strength): 기본 10, 높을수록 느리지만 안전

encode("1234") 호출 시:
  → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
       │   │   │                          │
       │   │   │                          └── 해시값
       │   │   └── Salt (22자)
       │   └── Cost Factor (10)
       └── 알고리즘 ($2a = BCrypt)
```

| 항목 | 설명 |
|------|------|
| **절대 금지** | 평문 저장, MD5, SHA-256 (Salt 없이) |
| **권장** | BCrypt, SCrypt, Argon2 |
| **Spring 기본** | BCryptPasswordEncoder |

---

## 권한 관리 (@PreAuthorize, @Secured, hasRole)

### 메서드 레벨 보안

```java
@Configuration
@EnableMethodSecurity  // 메서드 보안 활성화 (Spring Boot 3.x)
public class SecurityConfig {
    // ...
}
```

```java
@RestController
@RequestMapping("/api")
public class AdminController {

    // @PreAuthorize: SpEL 표현식 사용 (가장 유연, 권장)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // 여러 권한 중 하나라도 있으면 접근 허용
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/manage/reports")
    public List<Report> getReports() {
        return reportService.findAll();
    }

    // 파라미터 접근 가능 (SpEL)
    @PreAuthorize("#userId == authentication.principal.id")
    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    // @Secured: 단순 역할 검사 (SpEL 불가)
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

### URL 레벨 보안 (SecurityFilterChain)

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()           // 인증 없이 접근 가능
            .requestMatchers("/api/admin/**").hasRole("ADMIN")     // ADMIN만 접근
            .requestMatchers("/api/manage/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()  // GET만 허용
            .anyRequest().authenticated()                          // 나머지는 인증 필요
        );
    return http.build();
}
```

| 방식 | 어노테이션 | 특징 |
|------|-----------|------|
| **@PreAuthorize** | `@PreAuthorize("hasRole('ADMIN')")` | SpEL 표현식, 가장 유연, **권장** |
| **@Secured** | `@Secured("ROLE_ADMIN")` | 단순 역할 검사, SpEL 불가 |
| **@RolesAllowed** | `@RolesAllowed("ADMIN")` | JSR-250 표준, @Secured와 유사 |
| **URL 기반** | `requestMatchers().hasRole()` | SecurityFilterChain에서 설정 |

---

## Form Login 인증 흐름 (전체 과정)

```
[사용자] POST /login (username=hong, password=1234)
    │
    ▼
① [UsernamePasswordAuthenticationFilter]
    │  요청을 가로챔 (POST /login)
    │  UsernamePasswordAuthenticationToken 생성 (미인증 상태)
    │  token = new UsernamePasswordAuthenticationToken("hong", "1234")
    ▼
② [AuthenticationManager (ProviderManager)]
    │  등록된 AuthenticationProvider에게 인증 위임
    ▼
③ [DaoAuthenticationProvider]
    │  UserDetailsService.loadUserByUsername("hong") 호출
    ▼
④ [CustomUserDetailsService]
    │  DB에서 "hong" 사용자 조회
    │  UserDetails 객체 반환 (암호화된 비밀번호 포함)
    ▼
⑤ [DaoAuthenticationProvider]
    │  PasswordEncoder.matches("1234", encodedPassword) 비교
    │
    ├── 실패 → BadCredentialsException → AuthenticationFailureHandler
    │
    └── 성공 ↓
    ▼
⑥ [Authentication 객체 생성] (인증 완료 상태)
    │  UsernamePasswordAuthenticationToken (principal, null, authorities)
    │  * credentials(비밀번호)는 null로 설정 (보안)
    ▼
⑦ [SecurityContextHolder에 저장]
    │  SecurityContextHolder.getContext().setAuthentication(auth)
    ▼
⑧ [AuthenticationSuccessHandler]
    │  로그인 성공 후 처리 (리다이렉트, JSON 응답 등)
    ▼
⑨ [세션에 SecurityContext 저장]
    │  이후 요청에서 세션으로 인증 상태 유지
```

---

## JWT 인증 흐름 (Custom Filter 등록, OncePerRequestFilter)

### JWT 인증의 특징 (세션 vs JWT)

```
Session 기반 인증:
[클라이언트] ──Cookie: JSESSIONID──→ [서버: 세션 저장소에서 조회]
  → 서버에 상태 저장 (Stateful)
  → 서버 확장 시 세션 공유 문제

JWT 기반 인증:
[클라이언트] ──Authorization: Bearer eyJhb...──→ [서버: 토큰 자체를 검증]
  → 서버에 상태 없음 (Stateless)
  → 서버 확장 용이 (어떤 서버에서든 검증 가능)
```

### JWT 인증 흐름

```
[로그인 요청]  POST /api/auth/login  {email, password}
     │
     ▼
[AuthController]
     │  AuthenticationManager.authenticate() 호출
     │  인증 성공 시 TokenProvider.createToken() 호출
     ▼
[TokenProvider]
     │  JWT 생성 (Header.Payload.Signature)
     │  Access Token + (Refresh Token) 반환
     ▼
[클라이언트]  토큰 저장 (localStorage, Cookie 등)


─── 이후 API 요청 시 ───

[클라이언트]  GET /api/posts  (Authorization: Bearer eyJhbG...)
     │
     ▼
① [JwtAuthenticationFilter] (OncePerRequestFilter)
     │  Authorization 헤더에서 토큰 추출
     │  TokenProvider.validateToken(token) 검증
     │
     ├── 토큰 없음/유효하지 않음 → 필터 통과 (인증 없이 진행)
     │
     └── 토큰 유효 ↓
     ▼
② [TokenProvider]
     │  토큰에서 사용자 정보 추출 (Claims)
     │  UserDetailsService.loadUserByUsername() 호출
     ▼
③ [Authentication 객체 생성]
     │  UsernamePasswordAuthenticationToken 생성
     │  SecurityContextHolder에 저장
     ▼
④ [다음 Filter → Controller]
     │  SecurityContext에 인증 정보가 있으므로 인가 검사 통과
```

---

## Spring Security + JWT 구현 구조

### 1. TokenProvider (JWT 생성/검증)

```java
@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;  // 예: 3600000 (1시간)

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(authentication.getName())    // 사용자 식별자
                .claim("auth", authorities)              // 권한 정보
                .setIssuedAt(now)                        // 발행 시간
                .setExpiration(expiry)                    // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)  // 서명
                .compact();
    }

    // 토큰에서 Authentication 객체 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명
        } catch (ExpiredJwtException e) {
            // 만료된 JWT
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT
        } catch (IllegalArgumentException e) {
            // JWT 클레임이 비어있음
        }
        return false;
    }
}
```

### 2. JwtAuthenticationFilter (커스텀 필터)

```java
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 검증 및 인증 처리
        if (token != null && tokenProvider.validateToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 이후 토큰 부분
        }
        return null;
    }
}
```

### 3. SecurityConfig (보안 설정)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())

            // 세션 사용 안 함 (JWT는 Stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // URL별 접근 권한
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
            .addFilterBefore(
                new JwtAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 전체 구조 요약

```
Spring Security + JWT 구현 구조:

[SecurityConfig]
  ├── SecurityFilterChain 설정
  │     ├── CSRF 비활성화
  │     ├── Stateless 세션
  │     ├── CORS 설정
  │     ├── URL 접근 권한
  │     └── JwtAuthenticationFilter 등록
  │
  ├── PasswordEncoder (BCrypt)
  └── AuthenticationManager

[TokenProvider]
  ├── createToken()      → JWT 생성
  ├── getAuthentication() → 토큰 → Authentication
  └── validateToken()    → 토큰 검증

[JwtAuthenticationFilter] extends OncePerRequestFilter
  └── 매 요청마다 토큰 추출 → 검증 → SecurityContext 저장

[CustomUserDetailsService] implements UserDetailsService
  └── DB에서 사용자 조회 → UserDetails 반환

[AuthController]
  ├── POST /api/auth/signup  → 회원가입
  └── POST /api/auth/login   → 로그인 → JWT 발급
```

---

## CORS 설정

### CORS란?

```
CORS (Cross-Origin Resource Sharing): 교차 출처 리소스 공유

브라우저 보안 정책 중 하나인 SOP(Same-Origin Policy)를 우회하기 위한 메커니즘.

Origin = Protocol + Host + Port
http://localhost:3000  ≠  http://localhost:8080  → 다른 Origin!

[React App]                              [Spring Boot API]
http://localhost:3000                     http://localhost:8080
       │                                        │
       │── GET /api/posts ─────────────────→    │
       │                                        │
       │← Access-Control-Allow-Origin ────────  │
       │   "http://localhost:3000"               │
```

### Spring Security에서 CORS 설정

```java
// SecurityConfig에서 설정 (권장)
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "https://mydomain.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);  // Cookie 허용
    config.setMaxAge(3600L);           // Preflight 캐시 1시간

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

> **주의**: Spring Security 사용 시, `@CrossOrigin` 어노테이션만으로는 부족하다.
> Security Filter가 먼저 동작하므로 SecurityFilterChain 내에서 CORS를 설정해야 한다.

---

## CSRF란? SPA에서 CSRF 비활성화하는 이유

### CSRF (Cross-Site Request Forgery) 공격

```
CSRF 공격 시나리오:

① 사용자가 은행 사이트에 로그인 (세션 쿠키 발급)
② 공격자가 만든 악성 사이트 방문
③ 악성 사이트에 숨겨진 폼이 자동 제출:
   <form action="https://bank.com/transfer" method="POST">
     <input name="to" value="attacker">
     <input name="amount" value="1000000">
   </form>
④ 브라우저가 쿠키를 자동으로 함께 전송 → 이체 실행!
```

### SPA에서 CSRF를 비활성화하는 이유

```
Session 기반 (CSRF 필요):
  Cookie(JSESSIONID) → 브라우저가 자동 전송 → CSRF 공격 가능

JWT 기반 (CSRF 불필요):
  Authorization: Bearer eyJhb... → 수동으로 헤더에 추가 → CSRF 공격 불가능
  (악성 사이트에서 Authorization 헤더를 추가할 수 없음)
```

```java
// JWT 사용 시 CSRF 비활성화
http.csrf(csrf -> csrf.disable());

// Session 사용 시 CSRF 활성화 (기본값)
http.csrf(Customizer.withDefaults());
```

| 인증 방식 | CSRF 토큰 | 이유 |
|-----------|----------|------|
| **Session (Cookie)** | 필요 | 쿠키가 자동 전송되므로 CSRF 공격 가능 |
| **JWT (Header)** | 불필요 | 토큰을 수동으로 헤더에 추가하므로 CSRF 불가 |
| **JWT (Cookie 저장)** | 필요 | Cookie에 저장하면 자동 전송되므로 취약 |

---

## OAuth 2.0 / Social Login 기본 흐름

### OAuth 2.0 Authorization Code Grant 흐름

```
[사용자]     [우리 서버]      [인가 서버(Google)]    [리소스 서버(Google API)]
   │             │                   │                       │
   │── 소셜 로그인 ──→│                   │                       │
   │  클릭           │                   │                       │
   │             │── 인가 요청 ────→│                       │
   │             │  (client_id,     │                       │
   │             │   redirect_uri,  │                       │
   │             │   scope)         │                       │
   │←── 로그인 페이지 ─────────────│                       │
   │                                │                       │
   │── Google 로그인 ──────────→│                       │
   │                                │                       │
   │←── redirect_uri?code=abc ──│                       │
   │             │                   │                       │
   │             │── code + secret ──→│                       │
   │             │                   │                       │
   │             │←── Access Token ──│                       │
   │             │                   │                       │
   │             │── Access Token ──────────────────────→│
   │             │                                          │
   │             │←── 사용자 정보 (email, name) ──────────│
   │             │                                          │
   │             │ 우리 DB에 사용자 저장/조회                    │
   │             │ 우리 JWT 발급                              │
   │←── JWT ──│                                          │
```

### Spring Boot OAuth2 Client 설정

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            scope: profile_nickname, account_email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-authentication-method: client_secret_post
```

```java
// OAuth2 로그인 성공 후 처리
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId();  // "google"
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 기존 회원이면 조회, 신규면 가입
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        new User(email, name, provider, Role.USER)));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
```

---

## 면접 핵심 정리 Q&A

**Q: Spring Security의 인증 처리 흐름을 설명해주세요.**
> 사용자가 로그인 요청을 보내면 UsernamePasswordAuthenticationFilter가 요청을 가로채서
> UsernamePasswordAuthenticationToken을 생성합니다.
> 이 토큰은 AuthenticationManager(ProviderManager)에게 전달되고,
> AuthenticationProvider가 UserDetailsService를 통해 DB에서 사용자를 조회한 뒤
> PasswordEncoder로 비밀번호를 비교합니다.
> 인증에 성공하면 Authentication 객체가 생성되어 SecurityContextHolder에 저장됩니다.

**Q: JWT 인증 방식에서 Spring Security를 어떻게 연동하나요?**
> OncePerRequestFilter를 상속한 JwtAuthenticationFilter를 만들어
> UsernamePasswordAuthenticationFilter 앞에 등록합니다.
> 이 필터는 매 요청마다 Authorization 헤더에서 Bearer 토큰을 추출하고,
> TokenProvider로 토큰을 검증한 뒤
> 유효하면 Authentication 객체를 생성하여 SecurityContextHolder에 저장합니다.
> 세션을 사용하지 않으므로 SessionCreationPolicy.STATELESS로 설정합니다.

**Q: CSRF를 비활성화해도 되는 이유는 무엇인가요?**
> CSRF 공격은 브라우저가 쿠키를 자동으로 전송하는 특성을 악용합니다.
> JWT를 Authorization 헤더에 담아 전송하는 방식은
> 클라이언트가 수동으로 헤더를 추가해야 하므로 악성 사이트에서 조작할 수 없습니다.
> 따라서 JWT + Stateless 구조에서는 CSRF 토큰이 불필요하여 비활성화합니다.
> 다만 JWT를 Cookie에 저장하는 경우에는 CSRF 보호가 여전히 필요합니다.

**Q: @PreAuthorize와 @Secured의 차이는 무엇인가요?**
> @PreAuthorize는 SpEL(Spring Expression Language) 표현식을 지원하여
> hasRole, hasAuthority, 파라미터 접근 등 유연한 권한 검사가 가능합니다.
> @Secured는 단순히 역할명을 문자열로 지정하여 검사하며 SpEL을 사용할 수 없습니다.
> @PreAuthorize가 더 유연하므로 실무에서 주로 사용됩니다.
> 둘 다 사용하려면 @EnableMethodSecurity를 설정 클래스에 추가해야 합니다.

**Q: SecurityContextHolder는 어떻게 동작하나요?**
> SecurityContextHolder는 내부적으로 ThreadLocal을 사용하여
> 현재 스레드에 SecurityContext(Authentication 객체)를 저장합니다.
> 같은 스레드 내 어디서든 SecurityContextHolder.getContext().getAuthentication()으로
> 현재 인증된 사용자 정보에 접근할 수 있습니다.
> 요청이 끝나면 SecurityContextPersistenceFilter가 컨텍스트를 정리합니다.
> ThreadLocal 기반이므로 비동기 처리 시 별도 설정이 필요합니다.

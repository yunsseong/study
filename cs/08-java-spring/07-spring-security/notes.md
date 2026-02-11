# Spring Security

## 개념

```
Spring 기반 인증(Authentication) + 인가(Authorization) 프레임워크.

인증: "너 누구야?" → 로그인
인가: "너 이거 할 수 있어?" → 권한 확인

요청 흐름:
HTTP 요청 → [Security Filter Chain] → Controller
            ├── 인증 필터 (JWT, 세션 확인)
            ├── 인가 필터 (권한 확인)
            └── CSRF, CORS 필터
```

---

## SecurityFilterChain 설정

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (REST API는 Stateless)
            .csrf(csrf -> csrf.disable())

            // 세션 사용 안 함 (JWT 사용)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfig()))

            // 경로별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()      // 인증 없이 접근
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // ADMIN만
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .anyRequest().authenticated()                      // 나머지는 인증 필요
            )

            // JWT 필터 추가
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 해싱
    }
}
```

---

## JWT 인증 구현

### JWT 구조

```
Header.Payload.Signature

Header:  {"alg": "HS256", "typ": "JWT"}
Payload: {"userId": 1, "role": "USER", "exp": 1234567890}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

### JwtProvider

```java
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;   // 30분

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;  // 7일

    // 토큰 생성
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + accessExpiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
```

### JWT 인증 필터

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 검증 + 인증 객체 설정
        if (token != null && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserId(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                String.valueOf(userId));

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

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

### 로그인 / 회원가입 API

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(
            @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .role(Role.USER)
            .build();

        userRepository.save(user);
    }

    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("잘못된 이메일 또는 비밀번호"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("잘못된 이메일 또는 비밀번호");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return new TokenResponseDto(accessToken, refreshToken);
    }
}
```

---

## 권한 관리

```java
// 메서드 레벨 권한 체크
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/admin/users/{id}")
public void deleteUser(@PathVariable Long id) { }

// 현재 로그인한 유저 정보 가져오기
@GetMapping("/me")
public UserResponseDto getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
    Long userId = Long.parseLong(userDetails.getUsername());
    return userService.getUser(userId);
}
```

---

## 면접 예상 질문

1. **Spring Security의 동작 흐름은?**
   - HTTP 요청 → Security Filter Chain → 인증 → 인가 → Controller

2. **JWT의 장단점은?**
   - 장점: Stateless, 확장 용이 / 단점: 토큰 탈취 시 무효화 어려움, 크기 큼

3. **Access Token과 Refresh Token을 왜 분리하나요?**
   - Access: 짧은 만료(보안) / Refresh: 긴 만료(편의). 탈취 피해 최소화

4. **비밀번호를 어떻게 저장하나요?**
   - BCrypt로 해싱하여 저장. 평문 저장 절대 금지

5. **CORS란?**
   - 다른 도메인에서 API 호출 허용 정책. 프론트-백엔드 분리 시 필요

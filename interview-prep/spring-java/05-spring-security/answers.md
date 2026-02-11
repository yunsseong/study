# Spring Security 면접 질문 + 답변

## 기본 개념 (1~5)

**Q1.** Spring Security란 무엇이며, 왜 필요한가요?

> Spring Security는 Spring 기반 애플리케이션의 인증(Authentication)과 인가(Authorization)를 담당하는 보안 프레임워크입니다. Servlet Filter 기반으로 동작하여, 요청이 Controller에 도달하기 전에 보안 처리를 수행합니다. Spring Security 없이 직접 보안을 구현하면 모든 컨트롤러에 토큰 검증, 권한 확인 등의 반복 코드가 필요하고 실수 가능성이 높습니다. Spring Security를 사용하면 SecurityFilterChain에서 일괄적으로 보안 정책을 적용하고, `@PreAuthorize` 같은 어노테이션으로 선언적으로 권한을 관리할 수 있어 코드가 깔끔하고 안전해집니다.

**Q2.** Spring Security의 인증(Authentication) 처리 흐름을 설명해주세요. (AuthenticationManager → AuthenticationProvider → UserDetailsService)

> 사용자가 로그인 요청을 보내면 UsernamePasswordAuthenticationFilter가 요청을 가로채서 UsernamePasswordAuthenticationToken(미인증 상태)을 생성합니다. 이 토큰은 AuthenticationManager(인터페이스)의 구현체인 ProviderManager에게 전달됩니다. ProviderManager는 등록된 AuthenticationProvider 목록을 순회하며, DaoAuthenticationProvider가 UserDetailsService의 loadUserByUsername()을 호출하여 DB에서 사용자를 조회합니다. 반환된 UserDetails의 암호화된 비밀번호와 입력 비밀번호를 PasswordEncoder.matches()로 비교합니다. 인증에 성공하면 권한 정보가 포함된 Authentication 객체가 생성되어 SecurityContextHolder에 저장됩니다.

**Q3.** 인증(Authentication)과 인가(Authorization)의 차이를 설명해주세요.

> 인증(Authentication)은 "이 사용자가 누구인가?"를 확인하는 과정으로, ID/PW 검증이나 JWT 토큰 검증이 해당됩니다. 인증에 실패하면 401 Unauthorized 응답을 반환합니다. 인가(Authorization)는 "이 사용자가 해당 리소스에 접근할 수 있는가?"를 확인하는 과정으로, Role이나 Authority 기반의 권한 검사가 해당됩니다. 인가에 실패하면 403 Forbidden 응답을 반환합니다. 인증이 먼저 수행되고, 인증된 사용자에 대해 인가가 수행됩니다. Spring Security에서는 Authentication 객체로 인증을, hasRole(), @PreAuthorize 등으로 인가를 처리합니다.

**Q4.** SecurityContextHolder는 무엇이며, 어떻게 동작하나요?

> SecurityContextHolder는 현재 인증된 사용자의 보안 정보(SecurityContext)를 저장하는 컨테이너입니다. 내부적으로 ThreadLocal을 사용하여 현재 스레드에 SecurityContext를 저장하므로, 같은 요청(같은 스레드) 내 어디서든 `SecurityContextHolder.getContext().getAuthentication()`으로 현재 인증된 사용자 정보에 접근할 수 있습니다. SecurityContext 안에는 Authentication 객체가 있고, 이 객체에는 Principal(사용자 정보), Credentials(인증 수단), Authorities(권한 목록)가 포함됩니다. 요청이 끝나면 SecurityContextPersistenceFilter가 컨텍스트를 정리합니다. ThreadLocal 기반이므로 비동기 처리(@Async 등) 시에는 SecurityContext가 전파되지 않아 별도 설정이 필요합니다.

**Q5.** UserDetails와 UserDetailsService의 역할을 설명해주세요.

> UserDetails는 Spring Security가 사용자 정보를 표현하는 인터페이스로, getUsername(), getPassword(), getAuthorities() 등의 메서드를 제공합니다. 우리 DB의 User 엔티티를 Spring Security가 이해할 수 있는 형태로 변환하는 역할입니다. UserDetailsService는 username(식별자)으로 사용자를 조회하는 인터페이스로, loadUserByUsername() 메서드 하나만 가지고 있습니다. 실제 구현에서는 UserRepository를 통해 DB에서 사용자를 조회하고, 조회된 엔티티를 UserDetails 구현체로 감싸서 반환합니다. AuthenticationProvider가 인증 과정에서 이 서비스를 호출하여 사용자 정보를 가져옵니다.

## 비교/구분 (6~9)

**Q6.** Session 기반 인증과 JWT 기반 인증의 차이를 설명해주세요.

> Session 기반 인증은 서버가 세션 저장소에 인증 정보를 보관하고, 클라이언트에게 JSESSIONID 쿠키를 발급하는 Stateful 방식입니다. 서버가 상태를 유지하므로 서버 확장(Scale-out) 시 세션 공유 문제가 발생하고, Sticky Session이나 Redis 같은 별도 세션 저장소가 필요합니다. JWT 기반 인증은 서버가 상태를 저장하지 않는 Stateless 방식입니다. 토큰 자체에 사용자 정보와 권한이 포함되어 있어, 어떤 서버에서든 토큰만 검증하면 인증이 완료됩니다. 서버 확장이 용이하지만, 토큰이 탈취되면 만료 전까지 무효화하기 어렵다는 단점이 있어 Refresh Token 전략이 필요합니다.

**Q7.** @PreAuthorize와 @Secured의 차이를 설명해주세요.

> @PreAuthorize는 SpEL(Spring Expression Language) 표현식을 지원하여 유연한 권한 검사가 가능합니다. `hasRole('ADMIN')`, `hasAnyRole('ADMIN', 'MANAGER')`, 심지어 메서드 파라미터 접근(`#userId == authentication.principal.id`)까지 표현할 수 있습니다. @Secured는 단순히 역할명을 문자열로 지정하여 검사하며(`@Secured("ROLE_ADMIN")`), SpEL을 사용할 수 없어 복합 조건 표현이 불가능합니다. 두 어노테이션 모두 @EnableMethodSecurity 설정이 필요합니다. 실무에서는 유연성이 높은 @PreAuthorize를 주로 사용합니다.

**Q8.** CSRF란 무엇이며, JWT 사용 시 CSRF를 비활성화해도 되는 이유는 무엇인가요?

> CSRF(Cross-Site Request Forgery)는 사용자가 인증된 상태에서 악성 사이트가 사용자 모르게 요청을 보내는 공격입니다. 브라우저가 쿠키를 해당 도메인에 자동으로 전송하는 특성을 악용합니다. Session 기반 인증에서는 JSESSIONID 쿠키가 자동 전송되므로 CSRF 토큰으로 방어해야 합니다. 반면 JWT를 Authorization 헤더에 담아 전송하는 방식은, 클라이언트가 JavaScript로 수동으로 헤더를 추가해야 하므로 악성 사이트에서 이 헤더를 조작할 수 없습니다. 따라서 JWT + Stateless 구조에서는 CSRF 비활성화가 안전합니다. 다만 JWT를 Cookie에 저장하는 경우에는 CSRF 보호가 여전히 필요합니다.

**Q9.** Form Login 인증 흐름과 JWT 인증 흐름의 차이를 설명해주세요.

> Form Login은 UsernamePasswordAuthenticationFilter가 POST /login 요청을 가로채서 인증을 수행하고, 성공 시 세션에 SecurityContext를 저장하는 Stateful 방식입니다. 이후 요청에서는 세션 쿠키로 인증 상태를 유지합니다. JWT 인증은 로그인 성공 시 JWT 토큰을 발급하고, 이후 요청에서 OncePerRequestFilter를 상속한 JwtAuthenticationFilter가 Authorization 헤더에서 토큰을 추출하여 검증합니다. 유효한 토큰이면 Authentication 객체를 생성하여 SecurityContextHolder에 저장하는 Stateless 방식입니다. JWT 방식은 세션을 사용하지 않으므로 `SessionCreationPolicy.STATELESS`로 설정하고, JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록합니다.

## 심화/실무 (10~12)

**Q10.** Spring Security + JWT 구현 시 핵심 컴포넌트(TokenProvider, JwtAuthenticationFilter, SecurityConfig)의 역할과 관계를 설명해주세요.

> TokenProvider는 JWT 토큰의 생성(createToken), 검증(validateToken), 파싱(getAuthentication)을 담당하는 컴포넌트입니다. HMAC-SHA256 같은 알고리즘으로 서명하고, Claims에 사용자 식별자와 권한 정보를 담습니다. JwtAuthenticationFilter는 OncePerRequestFilter를 상속하여 매 요청마다 Authorization 헤더에서 Bearer 토큰을 추출하고, TokenProvider로 검증한 뒤 유효하면 Authentication 객체를 SecurityContextHolder에 저장합니다. SecurityConfig는 SecurityFilterChain을 구성하여 CSRF 비활성화, Stateless 세션, CORS 설정, URL별 접근 권한을 정의하고, JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록합니다. 이 세 컴포넌트가 협력하여 Stateless JWT 인증 체계를 구현합니다.

**Q11.** Spring Security에서 CORS를 설정하는 방법과 주의사항을 설명해주세요.

> Spring Security에서 CORS를 설정하려면 CorsConfigurationSource Bean을 등록하고, SecurityFilterChain에서 `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`로 활성화합니다. CorsConfiguration에 허용할 Origin, HTTP Method, Header, Credentials 여부 등을 지정합니다. 주의사항으로, Spring Security를 사용할 때 컨트롤러의 @CrossOrigin 어노테이션만으로는 충분하지 않습니다. Security Filter가 컨트롤러보다 먼저 동작하므로 Preflight 요청(OPTIONS)이 Security Filter에서 차단될 수 있기 때문입니다. 반드시 SecurityFilterChain 내에서 CORS 설정을 해야 하며, 운영 환경에서는 `*` 대신 구체적인 Origin을 지정해야 보안상 안전합니다.

**Q12.** OAuth 2.0 Authorization Code Grant 흐름을 설명해주세요.

> 사용자가 소셜 로그인 버튼을 클릭하면, 우리 서버가 Google 등 인가 서버로 인가 요청(client_id, redirect_uri, scope)을 보냅니다. 사용자는 인가 서버의 로그인 페이지에서 인증하고 동의하면, 인가 서버가 redirect_uri로 Authorization Code를 전달합니다. 우리 서버는 이 Code와 client_secret을 인가 서버에 보내서 Access Token을 발급받습니다. 이 Access Token으로 리소스 서버(Google API)에서 사용자 정보(email, name)를 가져옵니다. 가져온 정보로 우리 DB에 사용자를 저장/조회하고, 우리 서비스의 JWT를 발급하여 클라이언트에게 전달합니다. Spring Boot에서는 spring-boot-starter-oauth2-client와 DefaultOAuth2UserService를 활용하여 구현합니다.

## 꼬리질문 대비 (13~15)

**Q13.** JWT의 Access Token이 탈취되면 어떻게 대응하나요? Refresh Token 전략을 설명해주세요.

> JWT는 Stateless 특성상 발급된 토큰을 서버에서 즉시 무효화하기 어렵습니다. 이를 보완하기 위해 Access Token의 만료 시간을 짧게(15~30분) 설정하고, 별도의 Refresh Token(만료 7~14일)을 함께 발급합니다. Access Token이 만료되면 클라이언트가 Refresh Token으로 새 Access Token을 재발급 받습니다. Refresh Token은 서버 DB나 Redis에 저장하여 관리하므로, 탈취가 의심되면 서버에서 Refresh Token을 삭제하여 재발급을 차단할 수 있습니다. 추가적으로 Refresh Token Rotation(재발급 시 Refresh Token도 교체)을 적용하면 보안을 더 강화할 수 있습니다. 긴급 상황에서는 서명 키를 변경하여 모든 토큰을 무효화하는 방법도 있지만, 모든 사용자가 재로그인해야 하는 부작용이 있습니다.

**Q14.** OncePerRequestFilter를 사용하는 이유는 무엇이며, 일반 Filter와 어떤 차이가 있나요?

> 일반 Servlet Filter는 forward나 include 등으로 같은 요청이 내부적으로 여러 번 처리될 때 필터가 중복 실행될 수 있습니다. OncePerRequestFilter는 이름 그대로 하나의 HTTP 요청에 대해 단 한 번만 실행되는 것을 보장하는 Spring 제공 추상 클래스입니다. 내부적으로 request attribute에 이미 실행되었는지 플래그를 설정하여 중복 실행을 방지합니다. JWT 인증 필터에서 OncePerRequestFilter를 사용하는 이유는, 토큰 추출과 검증 과정이 한 요청에 한 번만 수행되어야 하기 때문입니다. 중복 실행되면 불필요한 검증이 반복되고, SecurityContext가 여러 번 설정되는 문제가 발생할 수 있습니다.

**Q15.** PasswordEncoder로 BCrypt를 사용하는 이유는 무엇이며, 다른 해시 알고리즘(MD5, SHA-256)과 어떤 차이가 있나요?

> MD5와 SHA-256은 속도가 빠른 범용 해시 알고리즘으로, 비밀번호 해싱에는 부적합합니다. 빠르기 때문에 GPU를 이용한 Brute Force 공격에 취약하고, Salt를 별도로 관리해야 하며, Rainbow Table 공격에도 노출됩니다. BCrypt는 비밀번호 해싱을 위해 설계된 알고리즘으로, 세 가지 장점이 있습니다. 첫째, 의도적으로 느린 해싱으로 Brute Force 공격을 방어합니다. 둘째, Salt를 자동으로 생성하고 해시값에 포함시켜 같은 비밀번호도 매번 다른 해시값이 나옵니다. 셋째, Cost Factor(기본 10)를 조절하여 하드웨어 발전에 맞춰 해싱 비용을 높일 수 있습니다. Spring Security는 BCryptPasswordEncoder를 기본으로 제공하며, 더 강력한 SCrypt나 Argon2도 지원합니다.

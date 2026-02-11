# Spring Security 면접 질문

## 기본 개념 (1~5)

**Q1.** Spring Security란 무엇이며, 왜 필요한가요?

**Q2.** Spring Security의 인증(Authentication) 처리 흐름을 설명해주세요. (AuthenticationManager → AuthenticationProvider → UserDetailsService)

**Q3.** 인증(Authentication)과 인가(Authorization)의 차이를 설명해주세요.

**Q4.** SecurityContextHolder는 무엇이며, 어떻게 동작하나요?

**Q5.** UserDetails와 UserDetailsService의 역할을 설명해주세요.

## 비교/구분 (6~9)

**Q6.** Session 기반 인증과 JWT 기반 인증의 차이를 설명해주세요.

**Q7.** @PreAuthorize와 @Secured의 차이를 설명해주세요.

**Q8.** CSRF란 무엇이며, JWT 사용 시 CSRF를 비활성화해도 되는 이유는 무엇인가요?

**Q9.** Form Login 인증 흐름과 JWT 인증 흐름의 차이를 설명해주세요.

## 심화/실무 (10~12)

**Q10.** Spring Security + JWT 구현 시 핵심 컴포넌트(TokenProvider, JwtAuthenticationFilter, SecurityConfig)의 역할과 관계를 설명해주세요.

**Q11.** Spring Security에서 CORS를 설정하는 방법과 주의사항을 설명해주세요.

**Q12.** OAuth 2.0 Authorization Code Grant 흐름을 설명해주세요.

## 꼬리질문 대비 (13~15)

**Q13.** JWT의 Access Token이 탈취되면 어떻게 대응하나요? Refresh Token 전략을 설명해주세요.

**Q14.** OncePerRequestFilter를 사용하는 이유는 무엇이며, 일반 Filter와 어떤 차이가 있나요?

**Q15.** PasswordEncoder로 BCrypt를 사용하는 이유는 무엇이며, 다른 해시 알고리즘(MD5, SHA-256)과 어떤 차이가 있나요?

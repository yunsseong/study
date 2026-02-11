package com.example.blog.global.config;

import com.example.blog.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [설계 포인트] Spring Security 설정
 *
 * Spring Security는 "필터 체인"으로 동작한다.
 * HTTP 요청이 들어오면 여러 개의 필터를 거쳐서 Controller에 도달한다.
 *
 *   요청 → [CORS 필터] → [JWT 필터] → [인가 필터] → Controller
 *
 * 이 클래스에서 하는 일:
 *   1. 어떤 URL은 누구나 접근 가능 (회원가입, 로그인, 목록 조회)
 *   2. 어떤 URL은 로그인 필수 (글 작성, 수정, 삭제)
 *   3. 세션 사용 안 함 (JWT 기반이니까)
 *   4. 우리가 만든 JwtAuthenticationFilter를 필터 체인에 끼워넣음
 *
 * Spring Boot 3.x에서는 WebSecurityConfigurerAdapter가 deprecated 되었고,
 * SecurityFilterChain Bean 방식을 사용한다. (면접에서 자주 물어봄)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화: REST API는 세션을 쓰지 않으므로 CSRF 공격이 불가능하다.
                // 브라우저 기반 세션 인증일 때만 CSRF 보호가 필요하다.
                .csrf(AbstractHttpConfigurer::disable)

                // H2 콘솔 접근을 위한 설정 (개발 환경 전용)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // 세션 사용 안 함: JWT를 쓰니까 서버에 세션을 저장할 필요가 없다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 URL들
                        .requestMatchers("/api/v1/auth/**").permitAll()      // 회원가입, 로그인
                        .requestMatchers("/h2-console/**").permitAll()        // H2 콘솔 (개발용)
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()     // 글 조회
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll() // 카테고리 조회

                        // 그 외 모든 요청은 인증 필수
                        .anyRequest().authenticated()
                )

                // JWT 필터를 Spring Security의 UsernamePasswordAuthenticationFilter 앞에 배치
                // → Spring의 기본 로그인 처리보다 먼저 JWT를 검사한다
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화기
     *
     * BCrypt: 단방향 해시 함수. 같은 비밀번호도 매번 다른 해시값이 나온다 (salt 때문).
     * DB에 비밀번호를 평문으로 저장하면 DB가 뚫렸을 때 끝장난다.
     * BCrypt로 암호화하면 원본을 복원할 수 없다 (단방향).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.example.blog.domain.auth.controller;

import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignupRequest;
import com.example.blog.domain.auth.dto.TokenRefreshRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 토큰 갱신 (인증 불필요)
     *
     * Access Token이 만료된 상태에서 호출하는 API이므로
     * 인증 없이 접근 가능해야 한다.
     * SecurityConfig에서 /api/v1/auth/** 는 permitAll이므로 통과된다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * 로그아웃 (인증 필수)
     *
     * 로그아웃은 "로그인한 사용자"만 할 수 있으므로 인증이 필요하다.
     * SecurityConfig에서 /api/v1/auth/** 는 permitAll인데, 로그아웃만 인증 필수?
     * → /api/v1/auth/logout은 permitAll이지만,
     *   Authentication 파라미터가 null이면 로그아웃할 대상이 없으므로
     *   Service에서 memberId를 꺼낼 수 없다.
     *
     * 더 엄밀하게 하려면 SecurityConfig에서 logout URL만 별도로 authenticated()를 걸 수 있다.
     * 여기서는 간단하게 처리한다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);
        return ResponseEntity.noContent().build();
    }
}

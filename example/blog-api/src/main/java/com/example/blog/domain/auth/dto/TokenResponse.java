package com.example.blog.domain.auth.dto;

/**
 * [설계 포인트] 이중 토큰 응답
 *
 * 프론트엔드에서의 저장 전략:
 *   - accessToken: 메모리(JavaScript 변수)에 저장. 가장 안전.
 *   - refreshToken: HttpOnly 쿠키 또는 안전한 저장소에 저장.
 *
 * localStorage에 토큰을 저장하면 XSS 공격에 취약하다.
 * 하지만 많은 프로젝트에서 편의상 localStorage를 사용하기도 한다.
 * 보안이 중요한 서비스라면 HttpOnly 쿠키를 사용해야 한다.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}

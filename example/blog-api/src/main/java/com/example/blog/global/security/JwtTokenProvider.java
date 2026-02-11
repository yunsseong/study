package com.example.blog.global.security;

import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * [설계 포인트] Access Token + Refresh Token 이중 토큰 전략
 *
 * 왜 토큰을 2개 쓰는가?
 *
 * [Access Token만 쓸 때의 딜레마]
 *   - 유효기간을 길게 (30일) → 탈취되면 30일간 악용 가능 (보안 위험)
 *   - 유효기간을 짧게 (30분) → 30분마다 재로그인 필요 (사용자 불편)
 *
 * [2개를 쓰면 해결]
 *   - Access Token: 30분 수명. API 요청에 사용. 탈취돼도 피해 기간 짧음.
 *   - Refresh Token: 14일 수명. Access Token 갱신에만 사용. DB에 저장되어 무효화 가능.
 *
 * 클라이언트 흐름:
 *   1. 로그인 → Access Token + Refresh Token 받음
 *   2. API 호출 시 Access Token 사용
 *   3. Access Token 만료 → 401 응답 받음
 *   4. Refresh Token으로 POST /api/v1/auth/refresh 호출
 *   5. 새 Access Token + 새 Refresh Token 받음 (토큰 로테이션)
 *   6. 2번부터 반복
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Access Token 생성 (기존 createToken 이름 변경)
     */
    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessExpiration);
    }

    /**
     * Refresh Token 생성
     *
     * Access Token과 구조는 동일하지만 유효기간이 훨씬 길다.
     * Refresh Token에도 memberId를 넣어서, 나중에 "누구의 토큰인지" 확인할 수 있다.
     */
    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshExpiration);
    }

    public Long getMemberId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Refresh Token 전용 검증
     * Access Token 검증과 에러 코드가 다르다 (프론트에서 구분하기 위해)
     */
    public boolean validateRefreshToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private String createToken(Long memberId, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

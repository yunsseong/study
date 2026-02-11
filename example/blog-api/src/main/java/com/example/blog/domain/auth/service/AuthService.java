package com.example.blog.domain.auth.service;

import com.example.blog.domain.auth.dto.LoginRequest;
import com.example.blog.domain.auth.dto.SignupRequest;
import com.example.blog.domain.auth.dto.TokenRefreshRequest;
import com.example.blog.domain.auth.dto.TokenResponse;
import com.example.blog.domain.auth.entity.RefreshToken;
import com.example.blog.domain.auth.repository.RefreshTokenRepository;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.member.repository.MemberRepository;
import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import com.example.blog.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.create(request.email(), encodedPassword, request.nickname());
        Member savedMember = memberRepository.save(member);

        return createTokenPair(savedMember.getId());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return createTokenPair(member.getId());
    }

    /**
     * [설계 포인트] 토큰 갱신 흐름 (가장 핵심적인 부분)
     *
     * 프론트엔드에서 이렇게 호출한다:
     *   POST /api/v1/auth/refresh
     *   Body: { "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
     *
     * 동작:
     *   1. 클라이언트가 보낸 Refresh Token의 JWT 서명/만료 검증
     *   2. DB에서 해당 토큰 조회 (DB에 없으면 = 이미 사용됐거나 로그아웃된 토큰)
     *   3. DB 레코드의 만료 시간 확인
     *   4. 기존 Refresh Token 삭제 + 새 토큰 쌍 발급 (토큰 로테이션)
     *
     * 토큰 로테이션(Token Rotation)이란?
     *   갱신할 때마다 Refresh Token도 새걸로 바꾸는 전략이다.
     *
     *   [로테이션 없이]
     *   로그인 → RT_1 발급 (14일 유효)
     *   7일 후 갱신 → 새 AT 발급, RT_1 그대로 사용
     *   해커가 RT_1 탈취 → 남은 7일간 계속 악용 가능
     *
     *   [로테이션 적용]
     *   로그인 → RT_1 발급
     *   7일 후 갱신 → 새 AT + RT_2 발급, RT_1 삭제
     *   해커가 RT_1 탈취 → 이미 삭제됨 → 사용 불가
     */
    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        // 1. JWT 자체 검증 (서명, 만료)
        jwtTokenProvider.validateRefreshToken(request.refreshToken());

        // 2. DB에서 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 3. DB 레코드의 만료 시간 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Long memberId = refreshToken.getMemberId();

        // 4. 기존 토큰 삭제 + 새 토큰 쌍 발급 (토큰 로테이션)
        refreshTokenRepository.deleteAllByMemberId(memberId);
        return createTokenPair(memberId);
    }

    /**
     * 로그아웃: 해당 회원의 모든 Refresh Token을 삭제한다.
     *
     * Access Token은 삭제할 수 없다 (서버가 저장하지 않으니까).
     * 하지만 Refresh Token을 삭제하면:
     *   - 현재 Access Token 만료 후 갱신이 불가능하다
     *   - 결과적으로 Access Token 수명(30분) 후 완전히 로그아웃된다
     *
     * 즉각적인 로그아웃이 필요하면 Access Token 블랙리스트 방식을 써야 하지만,
     * 그러면 Redis 같은 별도 저장소가 필요하다 (이 프로젝트 범위 밖).
     */
    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }

    /**
     * Access Token + Refresh Token 쌍 생성
     *
     * 회원가입, 로그인, 토큰 갱신에서 공통으로 사용한다.
     * 매번 새 Refresh Token을 만들어서 DB에 저장한다.
     */
    private TokenResponse createTokenPair(Long memberId) {
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshTokenStr = jwtTokenProvider.createRefreshToken(memberId);

        // Refresh Token을 DB에 저장
        long refreshExpirationMs = jwtTokenProvider.getRefreshExpiration();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.create(memberId, refreshTokenStr, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.of(accessToken, refreshTokenStr);
    }
}

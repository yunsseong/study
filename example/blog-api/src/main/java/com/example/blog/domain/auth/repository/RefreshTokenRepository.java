package com.example.blog.domain.auth.repository;

import com.example.blog.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * 특정 회원의 모든 Refresh Token 삭제
     *
     * 사용 시나리오:
     *   - 새 Refresh Token 발급 시 기존 토큰 삭제 (토큰 로테이션)
     *   - "모든 기기에서 로그아웃" 기능
     *   - 토큰 탈취 감지 시 해당 회원의 모든 토큰 무효화
     */
    void deleteAllByMemberId(Long memberId);
}

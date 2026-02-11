package com.example.blog.domain.auth.entity;

import com.example.blog.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [설계 포인트] Refresh Token을 왜 DB에 저장하는가?
 *
 * Access Token은 DB에 저장하지 않는다 (Stateless).
 * 그런데 Refresh Token은 DB에 저장한다. 왜?
 *
 * 1. 강제 로그아웃:
 *    - 사용자가 "모든 기기에서 로그아웃"을 누르면?
 *    - Access Token은 만료될 때까지 무효화할 수 없다 (서버가 기억 안 하니까).
 *    - 하지만 DB의 Refresh Token을 삭제하면, 새 Access Token을 못 받는다.
 *    - 기존 Access Token이 만료되면 자연스럽게 로그아웃 된다.
 *
 * 2. 토큰 탈취 감지:
 *    - 해커가 Refresh Token을 훔쳐서 사용하면?
 *    - 정상 사용자도 갱신을 시도한다 → 이미 사용된 토큰으로 갱신 시도.
 *    - "이 토큰은 이미 사용됨" → 탈취 감지 → 해당 회원의 모든 토큰 삭제.
 *
 * 3. 토큰 로테이션 (Token Rotation):
 *    - Refresh Token으로 갱신할 때마다 새 Refresh Token을 발급한다.
 *    - 이전 Refresh Token은 즉시 삭제한다.
 *    - 해커가 훔친 토큰은 한 번만 쓸 수 있다.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private RefreshToken(Long memberId, String token, LocalDateTime expiryDate) {
        this.memberId = memberId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public static RefreshToken create(Long memberId, String token, LocalDateTime expiryDate) {
        return new RefreshToken(memberId, token, expiryDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}

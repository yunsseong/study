package com.example.blog.domain.member.entity;

import com.example.blog.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [설계 포인트] 회원 엔티티
 *
 * Lombok 적용 비교 (Todo 프로젝트 vs 여기):
 *
 *   Todo:  protected Todo() {}           → 직접 작성
 *   여기:  @NoArgsConstructor(access = AccessLevel.PROTECTED)  → Lombok 자동 생성
 *
 *   Todo:  public Long getId() { return id; }  → 직접 작성
 *   여기:  @Getter  → Lombok 자동 생성
 *
 * @NoArgsConstructor(access = PROTECTED):
 *   protected 기본 생성자를 자동 생성한다.
 *   JPA 필수 조건(기본 생성자)을 만족시키면서, 외부에서 new Member()를 못하게 막는다.
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt로 암호화된 비밀번호가 저장된다

    @Column(nullable = false, length = 50)
    private String nickname;

    private Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public static Member create(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname);
    }
}

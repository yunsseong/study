package com.example.blog.domain.member.repository;

import com.example.blog.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [설계 포인트] 커스텀 쿼리 메서드
 *
 * JpaRepository 기본 메서드: findById, save, delete 등
 * 여기에 도메인에 필요한 쿼리를 추가한다.
 *
 * Spring Data JPA는 메서드 이름을 분석해서 쿼리를 자동 생성한다:
 *   findByEmail(String email)
 *   → SELECT * FROM members WHERE email = ?
 *
 *   existsByEmail(String email)
 *   → SELECT COUNT(*) > 0 FROM members WHERE email = ?
 *
 * Optional<Member>를 반환하는 이유:
 *   null을 직접 다루면 NullPointerException 위험이 있다.
 *   Optional은 "값이 있을 수도 없을 수도 있다"를 타입으로 표현한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}

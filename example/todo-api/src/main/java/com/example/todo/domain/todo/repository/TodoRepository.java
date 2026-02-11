package com.example.todo.domain.todo.repository;

import com.example.todo.domain.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [설계 포인트] Repository 인터페이스
 *
 * JpaRepository<Todo, Long>을 상속하면 기본 CRUD 메서드가 자동 제공된다:
 *   - save(), findById(), findAll(), deleteById() 등
 *
 * "이게 끝이야?" → 맞다. 기본 CRUD는 이것만으로 충분하다.
 *
 * 커스텀 쿼리가 필요하면 여기에 메서드를 추가한다:
 *   - List<Todo> findByCompleted(boolean completed);  ← 메서드 이름으로 쿼리 자동 생성
 *   - @Query("SELECT t FROM Todo t WHERE t.title LIKE %:keyword%")
 *     List<Todo> searchByTitle(@Param("keyword") String keyword);
 *
 * 실무 팁:
 *   - 단순 조회는 메서드 이름 쿼리로 충분
 *   - 복잡한 조건 검색은 QueryDSL을 사용 (별도 학습 필요)
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
}

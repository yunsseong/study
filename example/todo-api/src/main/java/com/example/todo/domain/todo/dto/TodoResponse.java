package com.example.todo.domain.todo.dto;

import com.example.todo.domain.todo.entity.Todo;

import java.time.LocalDateTime;

/**
 * [설계 포인트] 응답 DTO - Entity를 직접 반환하지 않는 이유
 *
 * Controller에서 Todo 엔티티를 그대로 JSON으로 반환하면:
 *
 * 1. 순환 참조: 엔티티 간 양방향 관계가 있으면 JSON 직렬화 시 무한 루프
 * 2. 불필요한 노출: DB 내부 구조(컬럼명, 관계)가 API에 그대로 드러남
 * 3. 변경 전파: DB 컬럼명을 바꾸면 API 응답도 바뀌어서 프론트가 깨짐
 * 4. 지연 로딩 에러: @ManyToOne 등 연관 엔티티가 세션 밖에서 접근되면 에러
 *
 * DTO로 변환하면 이 모든 문제가 해결된다.
 *
 * from() 정적 메서드 패턴:
 *   - Entity → DTO 변환 로직을 DTO 안에 두는 패턴
 *   - Service에서 TodoResponse.from(todo) 한 줄로 변환
 *   - 변환 로직이 여기저기 흩어지지 않는다
 */
public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}

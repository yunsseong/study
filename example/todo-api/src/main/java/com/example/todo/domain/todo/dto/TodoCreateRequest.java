package com.example.todo.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * [설계 포인트] 요청 DTO를 Entity와 분리하는 이유
 *
 * "그냥 Todo 엔티티를 Controller 파라미터로 받으면 안 되나?"
 * → 절대 안 된다. 이유:
 *
 * 1. 보안: 클라이언트가 id, createdAt 등 건드리면 안 되는 필드를 보낼 수 있다.
 *    Entity로 바로 받으면 이런 필드도 바인딩될 위험이 있다.
 *
 * 2. 검증 분리: 생성 시에는 title이 필수지만, 수정 시에는 선택일 수 있다.
 *    Entity 하나에 이 두 가지 검증을 다 넣을 수 없다.
 *
 * 3. API 스펙 변경 자유: 클라이언트에 보내는 필드와 DB 컬럼이 항상 같지 않다.
 *    DTO가 있으면 API 스펙과 DB 스키마를 독립적으로 변경할 수 있다.
 *
 * record를 사용한 이유:
 *   - 요청 DTO는 값을 담아서 전달하는 역할만 한다 (불변)
 *   - record는 이런 "데이터 캐리어"에 최적화된 문법이다
 */
public record TodoCreateRequest(

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
        String description
) {
}

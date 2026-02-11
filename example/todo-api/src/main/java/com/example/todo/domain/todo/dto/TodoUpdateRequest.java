package com.example.todo.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * [설계 포인트] 생성(Create)과 수정(Update) 요청 DTO를 분리하는 이유
 *
 * 처음에는 "필드가 비슷한데 왜 나누지?" 싶지만, 실무에서는 대부분 달라진다.
 *
 * 예시:
 *   - 생성: title, description 필수
 *   - 수정: title만 수정 가능, description은 수정 불가 (비즈니스 규칙 변경)
 *
 * 처음부터 분리해두면 이런 변경에 유연하게 대응할 수 있다.
 */
public record TodoUpdateRequest(

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
        String description
) {
}

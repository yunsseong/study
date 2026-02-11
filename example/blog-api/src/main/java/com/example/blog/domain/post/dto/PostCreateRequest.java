package com.example.blog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        Long categoryId  // 선택값: null이면 카테고리 없음
) {
}

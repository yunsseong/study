package com.example.blog.domain.post.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * [설계 포인트] 페이징 응답 DTO
 *
 * 왜 List<PostResponse>를 바로 반환하지 않는가?
 * 프론트엔드에서 페이징 UI를 그리려면 이 정보들이 필요하다:
 *   - 전체 게시글 수 (totalElements)
 *   - 전체 페이지 수 (totalPages)
 *   - 현재 페이지 번호 (currentPage)
 *   - 다음 페이지 존재 여부 (hasNext)
 *
 * Spring의 Page 객체를 그대로 반환하면 불필요한 필드가 너무 많다.
 * 필요한 것만 뽑아서 깔끔한 응답을 만든다.
 */
public record PostListResponse(
        List<PostResponse> posts,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {

    public static PostListResponse from(Page<PostResponse> page) {
        return new PostListResponse(
                page.getContent(),
                page.getNumber(),       // 현재 페이지 (0부터 시작)
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}

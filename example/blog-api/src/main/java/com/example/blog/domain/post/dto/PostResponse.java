package com.example.blog.domain.post.dto;

import com.example.blog.domain.post.entity.Post;

import java.time.LocalDateTime;

/**
 * [설계 포인트] 응답 DTO에 연관 엔티티 정보를 평탄화(flatten)해서 담는다.
 *
 * Entity를 그대로 반환하면:
 *   {
 *     "title": "...",
 *     "author": {                 ← 중첩 객체
 *       "id": 1,
 *       "email": "...",
 *       "password": "$2a$10$..." ← 비밀번호 노출!
 *     }
 *   }
 *
 * DTO로 변환하면:
 *   {
 *     "title": "...",
 *     "authorId": 1,              ← 필요한 정보만 평탄화
 *     "authorNickname": "홍길동"
 *   }
 *
 * 이점:
 *   - 민감한 정보(비밀번호) 노출 방지
 *   - 프론트엔드가 쓰기 편한 구조
 *   - API 스펙이 DB 구조에 종속되지 않음
 */
public record PostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getCategory() != null ? post.getCategory().getName() : null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}

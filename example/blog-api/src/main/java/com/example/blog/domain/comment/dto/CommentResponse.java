package com.example.blog.domain.comment.dto;

import com.example.blog.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getCreatedAt()
        );
    }
}

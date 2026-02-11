package com.example.blog.domain.comment.controller;

import com.example.blog.domain.comment.dto.CommentCreateRequest;
import com.example.blog.domain.comment.dto.CommentResponse;
import com.example.blog.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [설계 포인트] 댓글 URL 설계
 *
 * 댓글은 게시글에 종속된 리소스다. URL에 이 관계가 드러나야 한다.
 *   POST   /api/v1/posts/{postId}/comments  ← "이 게시글에 댓글 달기"
 *   GET    /api/v1/posts/{postId}/comments  ← "이 게시글의 댓글 목록"
 *
 * 삭제는 댓글 ID만 있으면 되므로 게시글 경로가 필요 없다:
 *   DELETE /api/v1/comments/{commentId}     ← "이 댓글 삭제"
 *
 * 이런 URL 설계를 "RESTful"하다고 한다.
 * 리소스 간의 관계가 URL 구조에 반영되어 있어서 직관적이다.
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> create(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(memberId, postId, request));
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> findByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Long commentId) {
        Long memberId = (Long) authentication.getPrincipal();
        commentService.delete(memberId, commentId);
        return ResponseEntity.noContent().build();
    }
}

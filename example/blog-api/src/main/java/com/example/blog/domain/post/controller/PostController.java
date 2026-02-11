package com.example.blog.domain.post.controller;

import com.example.blog.domain.post.dto.PostCreateRequest;
import com.example.blog.domain.post.dto.PostListResponse;
import com.example.blog.domain.post.dto.PostResponse;
import com.example.blog.domain.post.dto.PostUpdateRequest;
import com.example.blog.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * [설계 포인트] 인증된 사용자 정보를 Controller에서 꺼내는 방법
 *
 * JwtAuthenticationFilter에서 SecurityContext에 memberId를 저장했다.
 * Controller에서 Authentication 파라미터로 받으면 자동 주입된다.
 * authentication.getPrincipal()이 memberId(Long)를 반환한다.
 *
 * 실무에서는 @AuthenticationPrincipal 커스텀 어노테이션을 만들거나,
 * ArgumentResolver를 등록해서 더 깔끔하게 처리하기도 한다.
 * 여기서는 가장 기본적인 방식을 사용한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    /**
     * 글 작성 (인증 필수)
     *
     * Authentication에서 현재 로그인한 사용자의 memberId를 꺼낸다.
     */
    @PostMapping
    public ResponseEntity<PostResponse> create(
            Authentication authentication,
            @Valid @RequestBody PostCreateRequest request) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(memberId, request));
    }

    /**
     * 목록 조회 (인증 불필요)
     *
     * @PageableDefault: 클라이언트가 page/size를 안 보내면 사용할 기본값
     *   page=0, size=10, 최신순 정렬
     *
     * 프론트엔드 호출 예시:
     *   GET /api/v1/posts                       → 기본값 (1페이지, 10개, 최신순)
     *   GET /api/v1/posts?page=2&size=20         → 3페이지, 20개씩
     *   GET /api/v1/posts?sort=title,asc         → 제목 오름차순
     */
    @GetMapping
    public ResponseEntity<PostListResponse> findAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(postService.search(keyword, pageable));
        }
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(postService.update(memberId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Long id) {
        Long memberId = (Long) authentication.getPrincipal();
        postService.delete(memberId, id);
        return ResponseEntity.noContent().build();
    }
}

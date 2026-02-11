package com.example.blog.domain.post.service;

import com.example.blog.domain.category.entity.Category;
import com.example.blog.domain.category.repository.CategoryRepository;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.member.repository.MemberRepository;
import com.example.blog.domain.post.dto.*;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [설계 포인트] PostService가 다른 도메인의 Repository를 사용하는 것에 대하여
 *
 * PostService가 MemberRepository, CategoryRepository를 주입받고 있다.
 * "도메인끼리 의존하면 안 되는 거 아닌가?" 라고 생각할 수 있다.
 *
 * 이 규모의 프로젝트에서는 괜찮다.
 * 도메인 간 의존을 완전히 끊으려면 이벤트 기반 아키텍처나 도메인 서비스를 써야 하는데,
 * 이건 프로젝트 규모가 커졌을 때 고려할 문제다.
 * 작은 프로젝트에서 과하게 분리하면 오히려 복잡도만 올라간다 (Over-Engineering).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request) {
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Category category = findCategoryOrNull(request.categoryId());

        Post post = Post.create(request.title(), request.content(), author, category);
        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    /**
     * 페이징 조회
     *
     * Pageable은 Controller에서 자동 바인딩된다:
     *   GET /api/v1/posts?page=0&size=10&sort=createdAt,desc
     *   → page=0 (첫 페이지), size=10 (10개씩), sort=createdAt 내림차순
     *
     * 프론트엔드에서 보내는 파라미터가 자동으로 Pageable 객체가 된다.
     */
    public PostListResponse findAll(Pageable pageable) {
        Page<PostResponse> page = postRepository.findAllWithAuthor(pageable)
                .map(PostResponse::from);
        return PostListResponse.from(page);
    }

    /**
     * 검색 조회
     * keyword가 제목 또는 내용에 포함된 게시글을 검색한다.
     */
    public PostListResponse search(String keyword, Pageable pageable) {
        Page<PostResponse> page = postRepository.searchByKeyword(keyword, pageable)
                .map(PostResponse::from);
        return PostListResponse.from(page);
    }

    public PostResponse findById(Long id) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    /**
     * 게시글 수정 - 작성자만 수정 가능
     *
     * 왜 memberId를 파라미터로 받는가?
     *   - Controller에서 JWT 토큰으로 추출한 현재 로그인한 사용자의 ID
     *   - Service는 "이 사람이 작성자인지" 검증만 한다
     *   - Service가 JWT를 직접 다루지 않는다 (관심사 분리)
     */
    @Transactional
    public PostResponse update(Long memberId, Long postId, PostUpdateRequest request) {
        Post post = getPostOrThrow(postId);
        validateAuthor(post, memberId);

        Category category = findCategoryOrNull(request.categoryId());
        post.update(request.title(), request.content(), category);
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long memberId, Long postId) {
        Post post = getPostOrThrow(postId);
        validateAuthor(post, memberId);
        postRepository.delete(post);
    }

    // --- private 헬퍼 메서드 ---

    private Post getPostOrThrow(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * 작성자 검증
     * 글의 작성자가 아니면 AccessDenied 예외를 던진다.
     */
    private void validateAuthor(Post post, Long memberId) {
        if (!post.isAuthor(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 카테고리 조회 (null 허용)
     * categoryId가 null이면 카테고리 없는 게시글을 의미한다.
     */
    private Category findCategoryOrNull(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}

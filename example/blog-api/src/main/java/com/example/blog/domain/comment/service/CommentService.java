package com.example.blog.domain.comment.service;

import com.example.blog.domain.comment.dto.CommentCreateRequest;
import com.example.blog.domain.comment.dto.CommentResponse;
import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.comment.repository.CommentRepository;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.member.repository.MemberRepository;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse create(Long memberId, Long postId, CommentCreateRequest request) {
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.create(request.content(), post, author);
        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    public List<CommentResponse> findByPostId(Long postId) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        return commentRepository.findByPostIdWithAuthor(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isAuthor(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        commentRepository.delete(comment);
    }
}

package com.example.blog.domain.comment.repository;

import com.example.blog.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 댓글 목록 조회 (작성자 정보 fetch join)
     * 작성일 오름차순 정렬 (오래된 댓글이 위에)
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);
}

package com.example.blog.domain.post.repository;

import com.example.blog.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * [설계 포인트] JPQL과 fetch join
 *
 * 왜 단순 findAll() 대신 커스텀 쿼리를 쓰는가?
 *
 * Post를 조회하면 author(Member)와 category(Category)도 필요하다.
 * LAZY 로딩이라 기본적으로는 각각 별도 쿼리가 나간다:
 *   1. SELECT * FROM posts (게시글 10개 조회)
 *   2. SELECT * FROM members WHERE id = 1 (1번 글의 작성자)
 *   3. SELECT * FROM members WHERE id = 2 (2번 글의 작성자)
 *   ... → 총 21번 쿼리 (N+1 문제)
 *
 * fetch join을 쓰면:
 *   1. SELECT p.*, m.*, c.* FROM posts p
 *      JOIN members m ON p.member_id = m.id
 *      LEFT JOIN categories c ON p.category_id = c.id
 *   → 1번 쿼리로 전부 가져온다.
 *
 * LEFT JOIN vs JOIN:
 *   - JOIN: category가 없는 게시글은 결과에서 빠진다 (카테고리 필수)
 *   - LEFT JOIN: category가 없어도 게시글은 나온다 (카테고리 선택)
 *
 * CountQuery를 별도로 쓰는 이유:
 *   - 페이징할 때 전체 개수를 세는 COUNT 쿼리가 자동 실행된다.
 *   - COUNT에는 fetch join이 필요 없다 (개수만 세면 되니까)
 *   - 분리하면 COUNT 쿼리 성능이 좋아진다.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.category",
            countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.category " +
            "WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%",
            countQuery = "SELECT COUNT(p) FROM Post p " +
                    "WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.category WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithAuthor(@Param("id") Long id);
}

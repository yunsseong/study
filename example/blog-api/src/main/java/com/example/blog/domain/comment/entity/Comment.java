package com.example.blog.domain.comment.entity;

import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [설계 포인트] Comment의 연관관계
 *
 * Comment(N) : Post(1) → "여러 댓글이 하나의 게시글에 달린다"
 * Comment(N) : Member(1) → "여러 댓글이 한 명의 작성자에게 속한다"
 *
 * 양방향 vs 단방향:
 *   이 프로젝트에서는 모두 "단방향"으로 설정했다.
 *   Comment → Post (단방향): Comment가 Post를 안다.
 *   Post → Comment (없음): Post는 Comment 목록을 모른다.
 *
 *   왜 양방향을 안 쓰는가?
 *   - 양방향(@OneToMany): Post 안에 List<Comment>를 두는 것
 *   - 편리하지만 N+1 문제, 순환 참조, 관리 복잡도가 늘어난다.
 *   - 댓글 목록은 CommentRepository에서 직접 조회하면 된다.
 *   - 실무에서는 "꼭 필요할 때만" 양방향을 쓴다.
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    private Comment(String content, Post post, Member author) {
        this.content = content;
        this.post = post;
        this.author = author;
    }

    public static Comment create(String content, Post post, Member author) {
        return new Comment(content, post, author);
    }

    public boolean isAuthor(Long memberId) {
        return this.author.getId().equals(memberId);
    }
}

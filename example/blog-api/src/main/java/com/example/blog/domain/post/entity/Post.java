package com.example.blog.domain.post.entity;

import com.example.blog.domain.category.entity.Category;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [설계 포인트] 엔티티 연관관계 (JPA에서 가장 어렵고 중요한 부분)
 *
 * Post는 두 개의 N:1 관계를 가진다:
 *   - Post(N) : Member(1) → "여러 게시글이 한 명의 작성자에 속한다"
 *   - Post(N) : Category(1) → "여러 게시글이 하나의 카테고리에 속한다"
 *
 * @ManyToOne(fetch = LAZY):
 *   - EAGER(즉시 로딩): Post를 조회하면 Member도 같이 SELECT (기본값)
 *   - LAZY(지연 로딩): Post만 조회하고, member.getName()을 호출할 때 SELECT
 *
 *   왜 LAZY를 써야 하는가?
 *   게시글 100개를 조회할 때:
 *     EAGER: SELECT posts + 100번의 SELECT member → 101번 쿼리 (N+1 문제)
 *     LAZY: SELECT posts → 1번 쿼리. 필요할 때만 member 조회.
 *
 *   실무에서는 @ManyToOne, @OneToOne은 반드시 LAZY로 설정한다.
 *   필요한 연관 데이터는 fetch join으로 한 번에 가져온다.
 *
 * @JoinColumn(name = "member_id"):
 *   - DB에서 posts 테이블에 member_id 외래키 컬럼이 생긴다.
 *   - 안 쓰면 JPA가 알아서 이름을 만들지만, 명시하는 게 가독성이 좋다.
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")  // TEXT: 긴 본문을 위한 타입
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")  // nullable: 카테고리 없는 게시글 허용
    private Category category;

    private Post(String title, String content, Member author, Category category) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
    }

    public static Post create(String title, String content, Member author, Category category) {
        return new Post(title, content, author, category);
    }

    public void update(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    /**
     * 작성자 확인 메서드
     *
     * "이 글의 작성자가 맞는지" 확인하는 로직을 엔티티 안에 둔다.
     * Service에서 post.getAuthor().getId().equals(memberId) 이렇게 하면
     * 비즈니스 규칙이 Service에 흩어진다.
     *
     * 엔티티에 두면:
     *   - "작성자 확인"이라는 비즈니스 규칙이 Post 클래스 안에 캡슐화된다.
     *   - 여러 Service에서 재사용 가능하다.
     */
    public boolean isAuthor(Long memberId) {
        return this.author.getId().equals(memberId);
    }
}

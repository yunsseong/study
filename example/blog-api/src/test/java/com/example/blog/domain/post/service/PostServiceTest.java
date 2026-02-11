package com.example.blog.domain.post.service;

import com.example.blog.domain.category.repository.CategoryRepository;
import com.example.blog.domain.member.entity.Member;
import com.example.blog.domain.member.repository.MemberRepository;
import com.example.blog.domain.post.dto.PostCreateRequest;
import com.example.blog.domain.post.dto.PostResponse;
import com.example.blog.domain.post.dto.PostUpdateRequest;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.post.repository.PostRepository;
import com.example.blog.global.error.BusinessException;
import com.example.blog.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("create: 게시글 생성")
    class Create {

        @Test
        @DisplayName("성공: 카테고리 없이 게시글을 생성한다")
        void successWithoutCategory() {
            // given
            Member author = Member.create("test@test.com", "encoded", "테스터");
            given(memberRepository.findById(1L)).willReturn(Optional.of(author));

            PostCreateRequest request = new PostCreateRequest("제목", "내용", null);
            Post savedPost = Post.create("제목", "내용", author, null);
            given(postRepository.save(any(Post.class))).willReturn(savedPost);

            // when
            PostResponse response = postService.create(1L, request);

            // then
            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.content()).isEqualTo("내용");
            assertThat(response.authorNickname()).isEqualTo("테스터");
            assertThat(response.categoryName()).isNull();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 회원이면 예외를 던진다")
        void failMemberNotFound() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());
            PostCreateRequest request = new PostCreateRequest("제목", "내용", null);

            // when & then
            assertThatThrownBy(() -> postService.create(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("update: 게시글 수정")
    class Update {

        @Test
        @DisplayName("실패: 작성자가 아니면 접근 거부 예외를 던진다")
        void failNotAuthor() {
            // given - memberId=1인 작성자가 쓴 글을 memberId=2가 수정 시도
            Member author = Member.create("author@test.com", "encoded", "작성자");

            // 리플렉션으로 id 설정 (테스트 환경에서 id가 null이므로)
            setId(author, 1L);

            Post post = Post.create("원래 제목", "원래 내용", author, null);
            given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

            PostUpdateRequest request = new PostUpdateRequest("수정 제목", "수정 내용", null);

            // when & then - memberId=2로 수정 시도 → ACCESS_DENIED
            assertThatThrownBy(() -> postService.update(2L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("delete: 게시글 삭제")
    class Delete {

        @Test
        @DisplayName("실패: 존재하지 않는 게시글을 삭제하면 예외를 던진다")
        void failPostNotFound() {
            // given
            given(postRepository.findByIdWithAuthor(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.delete(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
        }
    }

    /**
     * 테스트 헬퍼: 리플렉션으로 엔티티의 id를 설정한다.
     *
     * 왜 필요한가?
     *   - Entity의 id는 DB가 자동 생성한다 (@GeneratedValue)
     *   - 단위 테스트에서는 DB가 없으므로 id가 null이다
     *   - isAuthor() 같은 메서드를 테스트하려면 id가 필요하다
     *   - 리플렉션으로 강제로 넣어주는 것
     */
    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }
}

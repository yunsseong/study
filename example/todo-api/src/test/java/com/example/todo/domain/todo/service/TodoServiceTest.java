package com.example.todo.domain.todo.service;

import com.example.todo.domain.todo.dto.TodoCreateRequest;
import com.example.todo.domain.todo.dto.TodoResponse;
import com.example.todo.domain.todo.dto.TodoUpdateRequest;
import com.example.todo.domain.todo.entity.Todo;
import com.example.todo.domain.todo.repository.TodoRepository;
import com.example.todo.global.error.BusinessException;
import com.example.todo.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * [설계 포인트] 단위 테스트 (Unit Test)
 *
 * 단위 테스트란?
 *   - 테스트 대상(TodoService)만 진짜로 실행하고
 *   - 의존 객체(TodoRepository)는 가짜(Mock)로 대체한다
 *   - DB, 네트워크 없이 순수 로직만 테스트
 *
 * 왜 Mock을 쓰는가?
 *   - DB 없이도 테스트가 돌아간다 (빠르다, CI 환경에서 유리)
 *   - "Repository가 이 값을 반환하면, Service는 이렇게 동작해야 한다"를
 *     명확하게 검증할 수 있다
 *
 * @ExtendWith(MockitoExtension.class):
 *   - Spring Context를 로드하지 않는다 (= 매우 빠르다)
 *   - @Mock, @InjectMocks 어노테이션을 활성화한다
 *
 * @Nested: 테스트를 메서드별로 그룹핑한다
 *   - IDE에서 트리 구조로 보여서 가독성이 좋다
 *
 * @DisplayName: 한글로 테스트 의도를 설명한다
 *   - 테스트가 실패했을 때 "어떤 기능이 깨졌는지" 바로 파악 가능
 */
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Nested
    @DisplayName("create: Todo 생성")
    class Create {

        @Test
        @DisplayName("성공: 유효한 요청이면 Todo를 생성하고 응답을 반환한다")
        void success() {
            // given - 테스트 준비 (이런 상황이 주어졌을 때)
            TodoCreateRequest request = new TodoCreateRequest("스프링 공부", "JPA 복습하기");
            Todo savedTodo = Todo.create("스프링 공부", "JPA 복습하기");

            // Mock 설정: repository.save()가 호출되면 savedTodo를 반환하라
            given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

            // when - 실행 (이 행동을 하면)
            TodoResponse response = todoService.create(request);

            // then - 검증 (이런 결과가 나와야 한다)
            assertThat(response.title()).isEqualTo("스프링 공부");
            assertThat(response.description()).isEqualTo("JPA 복습하기");
            assertThat(response.completed()).isFalse();

            // repository.save()가 정확히 1번 호출되었는지 검증
            then(todoRepository).should().save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("findAll: 전체 조회")
    class FindAll {

        @Test
        @DisplayName("성공: 저장된 Todo 목록을 반환한다")
        void success() {
            // given
            List<Todo> todos = List.of(
                    Todo.create("첫 번째 할 일", null),
                    Todo.create("두 번째 할 일", "설명 있음")
            );
            given(todoRepository.findAll()).willReturn(todos);

            // when
            List<TodoResponse> responses = todoService.findAll();

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).title()).isEqualTo("첫 번째 할 일");
            assertThat(responses.get(1).title()).isEqualTo("두 번째 할 일");
        }

        @Test
        @DisplayName("성공: Todo가 없으면 빈 리스트를 반환한다")
        void emptyList() {
            // given
            given(todoRepository.findAll()).willReturn(List.of());

            // when
            List<TodoResponse> responses = todoService.findAll();

            // then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById: 단건 조회")
    class FindById {

        @Test
        @DisplayName("성공: 존재하는 ID로 조회하면 Todo를 반환한다")
        void success() {
            // given
            Todo todo = Todo.create("스프링 공부", null);
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

            // when
            TodoResponse response = todoService.findById(1L);

            // then
            assertThat(response.title()).isEqualTo("스프링 공부");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회하면 BusinessException을 던진다")
        void notFound() {
            // given
            given(todoRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoService.findById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("update: Todo 수정")
    class Update {

        @Test
        @DisplayName("성공: 존재하는 Todo의 제목과 설명을 수정한다")
        void success() {
            // given
            Todo todo = Todo.create("원래 제목", "원래 설명");
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

            TodoUpdateRequest request = new TodoUpdateRequest("수정된 제목", "수정된 설명");

            // when
            TodoResponse response = todoService.update(1L, request);

            // then - 더티 체킹으로 값이 변경되었는지 검증
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.description()).isEqualTo("수정된 설명");
        }
    }

    @Nested
    @DisplayName("toggleComplete: 완료 상태 토글")
    class ToggleComplete {

        @Test
        @DisplayName("성공: 미완료 → 완료로 변경한다")
        void toggleToComplete() {
            // given
            Todo todo = Todo.create("할 일", null);
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

            // when
            TodoResponse response = todoService.toggleComplete(1L);

            // then
            assertThat(response.completed()).isTrue();
        }
    }

    @Nested
    @DisplayName("delete: Todo 삭제")
    class Delete {

        @Test
        @DisplayName("성공: 존재하는 Todo를 삭제한다")
        void success() {
            // given
            Todo todo = Todo.create("삭제할 할 일", null);
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

            // when
            todoService.delete(1L);

            // then - repository.delete()가 호출되었는지 검증
            then(todoRepository).should().delete(todo);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 Todo를 삭제하면 예외를 던진다")
        void notFound() {
            // given
            given(todoRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoService.delete(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }
}

package com.example.todo.domain.todo.service;

import com.example.todo.domain.todo.dto.TodoCreateRequest;
import com.example.todo.domain.todo.dto.TodoResponse;
import com.example.todo.domain.todo.dto.TodoUpdateRequest;
import com.example.todo.domain.todo.entity.Todo;
import com.example.todo.domain.todo.repository.TodoRepository;
import com.example.todo.global.error.BusinessException;
import com.example.todo.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [설계 포인트] Service 계층의 역할
 *
 * Controller → Service → Repository 3계층 구조에서
 * Service는 "비즈니스 로직"을 담당한다.
 *
 * 비즈니스 로직이란?
 *   - "어떤 조건일 때 어떻게 동작해야 하는가"
 *   - 예: "완료된 Todo는 수정할 수 없다" (지금은 없지만, 추후 추가될 수 있는 규칙)
 *
 * Controller에 비즈니스 로직을 넣으면 안 되는 이유:
 *   - 같은 로직을 다른 Controller (예: 관리자 API)에서도 써야 할 때 중복
 *   - 단위 테스트가 어렵다 (HTTP 요청/응답까지 같이 테스트해야 함)
 *
 * @Transactional(readOnly = true):
 *   - 클래스 레벨에 readOnly = true를 걸어두고
 *   - 쓰기 메서드에만 @Transactional을 다시 선언한다
 *   - 읽기 전용 트랜잭션은 JPA가 더티체킹을 하지 않아 성능에 유리하다
 */
@Service
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @Autowired를 필드에 붙이는 방식은 실무에서 지양한다. 이유:
     *   - 테스트 시 mock 객체를 주입하기 어렵다
     *   - 필드가 final이 아니라 불변성이 깨진다
     *   - 순환 참조를 컴파일 타임에 잡을 수 없다
     *
     * 생성자가 1개면 @Autowired 생략 가능 (Spring 4.3+)
     */
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Todo 생성
     *
     * @Transactional: 쓰기 작업이므로 readOnly가 아닌 트랜잭션 필요
     */
    @Transactional
    public TodoResponse create(TodoCreateRequest request) {
        Todo todo = Todo.create(request.title(), request.description());
        Todo savedTodo = todoRepository.save(todo);
        return TodoResponse.from(savedTodo);
    }

    /**
     * Todo 전체 조회
     *
     * 실무에서는 findAll()보다 페이징을 사용한다.
     * 데이터가 10만 건이면 전부 메모리에 올리니까.
     * 학습 후 Pageable을 적용해보는 것을 권장한다.
     */
    public List<TodoResponse> findAll() {
        return todoRepository.findAll()
                .stream()
                .map(TodoResponse::from)  // Entity → DTO 변환
                .toList();
    }

    /**
     * Todo 단건 조회
     *
     * findById()는 Optional을 반환한다.
     * 없으면 BusinessException을 던져서 GlobalExceptionHandler가 처리하게 한다.
     */
    public TodoResponse findById(Long id) {
        Todo todo = getTodoOrThrow(id);
        return TodoResponse.from(todo);
    }

    /**
     * Todo 수정
     *
     * JPA의 더티 체킹(Dirty Checking)으로 동작한다:
     *   1. DB에서 엔티티를 조회한다 (영속 상태)
     *   2. 엔티티의 값을 변경한다
     *   3. 트랜잭션이 끝날 때 JPA가 변경을 감지해서 UPDATE SQL을 실행한다
     *   → repository.save()를 다시 호출할 필요가 없다
     */
    @Transactional
    public TodoResponse update(Long id, TodoUpdateRequest request) {
        Todo todo = getTodoOrThrow(id);
        todo.update(request.title(), request.description());
        return TodoResponse.from(todo);
    }

    /**
     * Todo 완료 상태 토글
     */
    @Transactional
    public TodoResponse toggleComplete(Long id) {
        Todo todo = getTodoOrThrow(id);
        todo.toggleComplete();
        return TodoResponse.from(todo);
    }

    /**
     * Todo 삭제
     */
    @Transactional
    public void delete(Long id) {
        Todo todo = getTodoOrThrow(id);
        todoRepository.delete(todo);
    }

    /**
     * 공통 조회 + 예외 처리 메서드
     *
     * 같은 "조회 후 없으면 예외" 로직이 여러 메서드에서 반복되므로
     * private 메서드로 추출했다.
     *
     * 실무에서 이런 반복 패턴을 발견하면 바로 메서드로 추출하는 습관을 들여라.
     */
    private Todo getTodoOrThrow(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    }
}

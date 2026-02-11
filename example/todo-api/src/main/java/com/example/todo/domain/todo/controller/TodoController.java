package com.example.todo.domain.todo.controller;

import com.example.todo.domain.todo.dto.TodoCreateRequest;
import com.example.todo.domain.todo.dto.TodoResponse;
import com.example.todo.domain.todo.dto.TodoUpdateRequest;
import com.example.todo.domain.todo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [설계 포인트] Controller의 역할
 *
 * Controller는 "HTTP 요청을 받아서 Service에 위임하고, 응답을 만드는 것"만 한다.
 *
 * Controller에서 하면 안 되는 것들:
 *   ❌ repository.findById()  → Service를 거쳐야 한다
 *   ❌ if (todo == null) throw new ...  → Service에서 처리
 *   ❌ todo.setTitle(request.getTitle())  → Service에서 처리
 *
 * Controller에서 해야 하는 것들:
 *   ✅ HTTP 메서드 매핑 (GET, POST, PUT, DELETE)
 *   ✅ 요청 파라미터 바인딩 (@RequestBody, @PathVariable)
 *   ✅ 입력값 검증 트리거 (@Valid)
 *   ✅ HTTP 상태 코드 결정
 *
 * @RestController = @Controller + @ResponseBody
 *   - 모든 메서드의 반환값이 JSON으로 직렬화된다
 *
 * @RequestMapping("/api/v1/todos")
 *   - URL에 버전(v1)을 넣는 이유:
 *     API 스펙이 바뀔 때 v2를 만들면 기존 클라이언트(v1)가 안 깨진다
 *     실무에서 모바일 앱은 강제 업데이트가 어려워서 이 패턴이 필수
 */
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * POST /api/v1/todos - Todo 생성
     *
     * @Valid: TodoCreateRequest의 @NotBlank, @Size 검증을 실행한다.
     *   검증 실패 시 MethodArgumentNotValidException이 발생하고,
     *   GlobalExceptionHandler에서 잡아서 에러 응답을 만든다.
     *
     * ResponseEntity: HTTP 상태 코드를 명시적으로 제어한다.
     *   201 Created: "새로운 리소스가 생성됨"을 의미. 단순 200 OK보다 의미가 정확하다.
     */
    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoCreateRequest request) {
        TodoResponse response = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/todos - 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<TodoResponse>> findAll() {
        return ResponseEntity.ok(todoService.findAll());
    }

    /**
     * GET /api/v1/todos/{id} - 단건 조회
     *
     * @PathVariable: URL의 {id} 부분을 파라미터로 바인딩
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.findById(id));
    }

    /**
     * PUT /api/v1/todos/{id} - 수정
     *
     * PUT vs PATCH:
     *   - PUT: 리소스 전체를 교체 (모든 필드를 보내야 함)
     *   - PATCH: 리소스 일부만 수정 (변경된 필드만 보내면 됨)
     *   여기서는 title + description 전체를 받으므로 PUT이 적합하다.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TodoUpdateRequest request) {
        return ResponseEntity.ok(todoService.update(id, request));
    }

    /**
     * PATCH /api/v1/todos/{id}/toggle - 완료 상태 토글
     *
     * 왜 별도 엔드포인트인가?
     *   - "완료 토글"은 "수정"과 다른 비즈니스 행위다
     *   - 프론트에서 체크박스 클릭 한 번으로 호출할 수 있다
     *   - PUT /todos/{id} 에 { completed: true }를 보내는 것보다 의도가 명확하다
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponse> toggleComplete(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.toggleComplete(id));
    }

    /**
     * DELETE /api/v1/todos/{id} - 삭제
     *
     * 204 No Content: "성공했지만 응답 본문이 없음"
     *   삭제 후 돌려줄 데이터가 없으므로 204가 적절하다.
     *
     * 실무 팁: 실제 운영에서는 물리 삭제(DELETE) 대신
     *   논리 삭제(deleted = true)를 사용하는 경우가 많다.
     *   데이터 복구, 감사 추적 등의 이유.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

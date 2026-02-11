package com.example.todo.domain.todo.controller;

import com.example.todo.domain.todo.dto.TodoCreateRequest;
import com.example.todo.domain.todo.dto.TodoResponse;
import com.example.todo.domain.todo.dto.TodoUpdateRequest;
import com.example.todo.domain.todo.service.TodoService;
import com.example.todo.global.error.BusinessException;
import com.example.todo.global.error.ErrorCode;
import com.example.todo.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [설계 포인트] Controller 슬라이스 테스트
 *
 * @WebMvcTest: Controller 계층만 로드하는 "슬라이스 테스트"
 *   - 전체 Spring Context를 로드하지 않아 빠르다
 *   - Controller + 예외 핸들러 + Validation만 테스트
 *   - Service는 @MockitoBean으로 가짜를 주입
 *
 * 단위 테스트(ServiceTest)와의 차이:
 *   - ServiceTest: 순수 Java 로직 테스트 (Spring 없음)
 *   - ControllerTest: HTTP 요청/응답, 상태코드, JSON 형식 테스트
 *
 * MockMvc: 실제 서버를 띄우지 않고 HTTP 요청을 시뮬레이션한다
 */
@WebMvcTest(controllers = TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Java 객체 ↔ JSON 변환

    @MockitoBean
    private TodoService todoService;

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0);

    @Nested
    @DisplayName("POST /api/v1/todos")
    class CreateTodo {

        @Test
        @DisplayName("성공: 201 Created와 생성된 Todo를 반환한다")
        void success() throws Exception {
            // given
            TodoCreateRequest request = new TodoCreateRequest("스프링 공부", "JPA 복습");
            TodoResponse response = new TodoResponse(1L, "스프링 공부", "JPA 복습", false, NOW, NOW);
            given(todoService.create(any(TodoCreateRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())     // 요청/응답 상세 출력 (디버깅용)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("스프링 공부"))
                    .andExpect(jsonPath("$.completed").value(false));
        }

        @Test
        @DisplayName("실패: 제목이 비어있으면 400 Bad Request")
        void validationFail_blankTitle() throws Exception {
            // given - 제목이 빈 문자열인 요청
            TodoCreateRequest request = new TodoCreateRequest("", null);

            // when & then
            mockMvc.perform(post("/api/v1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("C001"))
                    .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
        }

        @Test
        @DisplayName("실패: 제목이 200자를 초과하면 400 Bad Request")
        void validationFail_titleTooLong() throws Exception {
            // given
            String longTitle = "a".repeat(201);
            TodoCreateRequest request = new TodoCreateRequest(longTitle, null);

            // when & then
            mockMvc.perform(post("/api/v1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/todos")
    class FindAllTodos {

        @Test
        @DisplayName("성공: Todo 목록을 반환한다")
        void success() throws Exception {
            // given
            List<TodoResponse> responses = List.of(
                    new TodoResponse(1L, "첫 번째", null, false, NOW, NOW),
                    new TodoResponse(2L, "두 번째", "설명", true, NOW, NOW)
            );
            given(todoService.findAll()).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/v1/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("첫 번째"))
                    .andExpect(jsonPath("$[1].completed").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/todos/{id}")
    class FindTodoById {

        @Test
        @DisplayName("성공: 존재하는 ID로 조회하면 Todo를 반환한다")
        void success() throws Exception {
            // given
            TodoResponse response = new TodoResponse(1L, "스프링 공부", null, false, NOW, NOW);
            given(todoService.findById(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/todos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("스프링 공부"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID면 404 Not Found")
        void notFound() throws Exception {
            // given
            given(todoService.findById(999L))
                    .willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/todos/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("T001"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/todos/{id}")
    class UpdateTodo {

        @Test
        @DisplayName("성공: Todo를 수정하고 수정된 결과를 반환한다")
        void success() throws Exception {
            // given
            TodoUpdateRequest request = new TodoUpdateRequest("수정된 제목", "수정된 설명");
            TodoResponse response = new TodoResponse(1L, "수정된 제목", "수정된 설명", false, NOW, NOW);
            given(todoService.update(eq(1L), any(TodoUpdateRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/todos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/todos/{id}/toggle")
    class ToggleComplete {

        @Test
        @DisplayName("성공: 완료 상태를 토글한다")
        void success() throws Exception {
            // given
            TodoResponse response = new TodoResponse(1L, "할 일", null, true, NOW, NOW);
            given(todoService.toggleComplete(1L)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/todos/1/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/todos/{id}")
    class DeleteTodo {

        @Test
        @DisplayName("성공: Todo를 삭제하고 204 No Content를 반환한다")
        void success() throws Exception {
            // given
            willDoNothing().given(todoService).delete(1L);

            // when & then
            mockMvc.perform(delete("/api/v1/todos/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 Todo를 삭제하면 404")
        void notFound() throws Exception {
            // given
            willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND))
                    .given(todoService).delete(999L);

            // when & then
            mockMvc.perform(delete("/api/v1/todos/999"))
                    .andExpect(status().isNotFound());
        }
    }
}

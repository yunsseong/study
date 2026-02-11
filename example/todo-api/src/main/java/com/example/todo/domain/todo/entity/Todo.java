package com.example.todo.domain.todo.entity;

import com.example.todo.global.common.BaseEntity;
import jakarta.persistence.*;

/**
 * [설계 포인트] Entity 클래스 설계 원칙
 *
 * 1. Entity는 DB 테이블과 1:1 매핑되는 핵심 도메인 객체다.
 *    절대로 Controller에서 직접 반환하지 않는다. (→ DTO를 사용)
 *
 * 2. Setter를 열어두지 않는다.
 *    - setTitle("새 제목") 이렇게 아무 데서나 값을 바꿀 수 있으면
 *      "누가, 언제, 왜 바꿨는지" 추적이 불가능하다.
 *    - 대신 의미 있는 메서드를 만든다: update(), complete() 등
 *    - 이 메서드 이름만 봐도 "어떤 비즈니스 행위인지" 알 수 있다.
 *
 * 3. 생성자도 public으로 열어두지 않고, 정적 팩토리 메서드(create)를 사용한다.
 *    - new Todo(title) 보다 Todo.create(title)이 의도가 명확하다.
 *    - 생성 시 필요한 초기화 로직을 한 곳에서 관리할 수 있다.
 */
@Entity
@Table(name = "todos")
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    /**
     * JPA는 기본 생성자가 필수다. (리플렉션으로 객체를 생성하기 때문)
     * protected로 막아서 외부에서 new Todo()로 빈 객체를 못 만들게 한다.
     */
    protected Todo() {
    }

    private Todo(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
    }

    /**
     * 정적 팩토리 메서드: 새로운 Todo 생성
     * 생성 시 completed는 항상 false로 시작하는 것이 비즈니스 규칙이다.
     */
    public static Todo create(String title, String description) {
        return new Todo(title, description);
    }

    /**
     * Todo 내용 수정
     * Setter 대신 이 메서드를 호출하게 하면:
     * - "수정"이라는 비즈니스 행위가 코드에 드러난다
     * - 나중에 "수정 시 검증 로직"을 추가해도 이 메서드 하나만 고치면 된다
     */
    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * 완료 상태 토글
     * setCompleted(true/false) 대신 toggle을 쓰면
     * 프론트에서 "현재 상태를 몰라도" 호출할 수 있다.
     */
    public void toggleComplete() {
        this.completed = !this.completed;
    }

    // --- Getter만 제공 (Setter 없음) ---

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }
}
